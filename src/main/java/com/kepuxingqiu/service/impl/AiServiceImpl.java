package com.kepuxingqiu.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepuxingqiu.common.exception.BusinessException;
import com.kepuxingqiu.common.result.ResultCode;
import com.kepuxingqiu.common.utils.RedisUtils;
import com.kepuxingqiu.dto.AiChatDTO;
import com.kepuxingqiu.entity.GameContent;
import com.kepuxingqiu.entity.User;
import com.kepuxingqiu.mapper.GameContentMapper;
import com.kepuxingqiu.mapper.UserMapper;
import com.kepuxingqiu.service.AiService;
import com.kepuxingqiu.vo.AiChatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private static final String SESSION_KEY_PREFIX = "ai:session:";
    private static final int    MAX_HISTORY        = 10;
    private static final long   SESSION_TTL_MINUTES = 30;
    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final UserMapper userMapper;
    private final GameContentMapper gameContentMapper;
    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${kepu.ai.api-key}")
    private String apiKey;

    @Value("${kepu.ai.model:qwen-turbo}")
    private String model;

    public AiServiceImpl(UserMapper userMapper, GameContentMapper gameContentMapper,
                         RedisUtils redisUtils, ObjectMapper objectMapper,
                         RestTemplate restTemplate) {
        this.userMapper = userMapper;
        this.gameContentMapper = gameContentMapper;
        this.redisUtils = redisUtils;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public AiChatVO chat(Long userId, AiChatDTO dto) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ResultCode.SERVER_ERROR, "AI服务未配置，请联系管理员");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        String sessionKey = SESSION_KEY_PREFIX + userId;
        List<String> historyJsonList = redisUtils.lRange(sessionKey, 0, -1);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(user, dto)));
        for (String json : historyJsonList) {
            try {
                messages.add(objectMapper.readValue(json, new TypeReference<Map<String, String>>() {}));
            } catch (Exception e) {
                log.warn("跳过无效的历史消息: {}", json);
            }
        }
        messages.add(Map.of("role", "user", "content", dto.getMessage()));

        String reply = callDashScope(messages);

        saveToSession(sessionKey, dto.getMessage(), reply);

        AiChatVO vo = new AiChatVO();
        vo.setReply(reply);
        vo.setHistoryCount((int) (redisUtils.lLen(sessionKey) / 2));
        return vo;
    }

    @Override
    public void clearSession(Long userId) {
        redisUtils.delete(SESSION_KEY_PREFIX + userId);
    }

    private void saveToSession(String sessionKey, String userMsg, String assistantReply) {
        try {
            redisUtils.lRightPush(sessionKey,
                    objectMapper.writeValueAsString(Map.of("role", "user", "content", userMsg)));
            redisUtils.lRightPush(sessionKey,
                    objectMapper.writeValueAsString(Map.of("role", "assistant", "content", assistantReply)));
            long len = redisUtils.lLen(sessionKey);
            if (len > MAX_HISTORY) {
                redisUtils.lTrim(sessionKey, len - MAX_HISTORY, -1);
            }
            redisUtils.expire(sessionKey, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("保存AI会话历史失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String callDashScope(List<Map<String, String>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("max_tokens", 800);
        body.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(DASHSCOPE_URL, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.error("调用DashScope API失败", e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "AI服务暂时不可用，请稍后再试");
        }
    }

    private String buildSystemPrompt(User user, AiChatDTO dto) {
        String ageDesc = switch (user.getAgeGroup() != null ? user.getAgeGroup() : 2) {
            case 1  -> "幼儿园小朋友（3-6岁），请用最简单的词语，多打比方，句子要短";
            case 2  -> "小学生（7-12岁），用通俗易懂的语言，可以适当使用比喻";
            default -> "初中生（13-15岁），可以适当使用科学术语并加以解释";
        };

        StringBuilder sb = new StringBuilder();
        sb.append("你是星球小助手，一个专为儿童设计的科普教育助手。");
        sb.append(String.format("当前用户是%s，等级%d级。", ageDesc, user.getLevel() != null ? user.getLevel() : 1));
        sb.append("只回答与自然科学、历史文明、天文地理、昆虫动植物等科普教育相关的问题，");
        sb.append("若用户问与科普无关的内容，请友好地引导回科普话题。");
        sb.append("回答简洁生动，每次不超过200字。");

        if ("GAME_HINT".equals(dto.getContextType()) && dto.getContextId() != null) {
            GameContent gc = gameContentMapper.selectById(dto.getContextId());
            if (gc != null) {
                sb.append(String.format("当前关卡主题：%s。知识点：%s。", gc.getTitle(), gc.getKnowledgePoint()));
                sb.append("请给出游戏提示，引导用户思考，但不要直接告诉答案。");
            }
        } else if ("ENCYCLOPEDIA".equals(dto.getContextType()) && dto.getContextId() != null) {
            sb.append("用户正在查看图鉴，请用生动的语言扩展讲解相关知识，激发好奇心。");
        }

        return sb.toString();
    }
}
