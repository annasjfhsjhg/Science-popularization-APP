package com.kepuxingqiu.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepuxingqiu.common.exception.BusinessException;
import com.kepuxingqiu.common.utils.RedisUtils;
import com.kepuxingqiu.dto.AiChatDTO;
import com.kepuxingqiu.entity.GameContent;
import com.kepuxingqiu.entity.User;
import com.kepuxingqiu.mapper.GameContentMapper;
import com.kepuxingqiu.mapper.UserMapper;
import com.kepuxingqiu.vo.AiChatVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiServiceImpl 单元测试")
class AiServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private GameContentMapper gameContentMapper;
    @Mock private RedisUtils redisUtils;
    @Mock private RestTemplate restTemplate;

    private AiServiceImpl aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(userMapper, gameContentMapper, redisUtils, objectMapper, restTemplate);
        ReflectionTestUtils.setField(aiService, "apiKey", "sk-test-key");
        ReflectionTestUtils.setField(aiService, "model", "qwen-turbo");
    }

    // ------------------------------------------------------------------
    //  工具方法
    // ------------------------------------------------------------------

    private User buildUser(int ageGroup, int level) {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setAgeGroup(ageGroup);
        user.setLevel(level);
        return user;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map> mockApiResponse(String content) {
        Map<String, Object> message = Map.of("content", content);
        Map<String, Object> choice  = Map.of("message", message);
        Map<String, Object> body    = Map.of("choices", List.of(choice));
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private AiChatDTO buildDto(String message, String contextType, Long contextId) {
        AiChatDTO dto = new AiChatDTO();
        dto.setMessage(message);
        dto.setContextType(contextType);
        dto.setContextId(contextId);
        return dto;
    }

    /** 从 RestTemplate 捕获的 HttpEntity 中取出 messages 列表 */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> captureMessages(ArgumentCaptor<Object> captor) {
        HttpEntity<Map<String, Object>> entity = (HttpEntity<Map<String, Object>>) captor.getValue();
        return (List<Map<String, String>>) entity.getBody().get("messages");
    }

    // ------------------------------------------------------------------
    //  chat() —— 正常流程
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("chat() 正常流程")
    class ChatSuccess {

        @BeforeEach
        void commonStubs() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            when(redisUtils.lRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptyList());
            when(redisUtils.lLen(anyString())).thenReturn(2L);
            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenReturn(mockApiResponse("天空因瑞利散射而呈蓝色"));
        }

        @Test
        @DisplayName("返回 DashScope 的回复内容")
        void returnsReply() {
            AiChatVO result = aiService.chat(1L, buildDto("为什么天空是蓝色的？", "GENERAL", null));
            assertThat(result.getReply()).isEqualTo("天空因瑞利散射而呈蓝色");
        }

        @Test
        @DisplayName("historyCount 等于 lLen / 2")
        void returnsCorrectHistoryCount() {
            when(redisUtils.lLen(anyString())).thenReturn(6L);
            AiChatVO result = aiService.chat(1L, buildDto("问题", "GENERAL", null));
            assertThat(result.getHistoryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("每次对话后将 user 和 assistant 两条消息写入 Redis")
        void savesConversationToRedis() {
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(redisUtils, times(2)).lRightPush(eq("ai:session:1"), anyString());
            verify(redisUtils).expire(eq("ai:session:1"), eq(30L), any());
        }

        @Test
        @DisplayName("历史超过 10 条时触发 lTrim 保留最新 10 条")
        void trimsHistoryWhenExceedsMax() {
            when(redisUtils.lLen(anyString())).thenReturn(12L);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            // lTrim(key, 12-10=2, -1)
            verify(redisUtils).lTrim(eq("ai:session:1"), eq(2L), eq(-1L));
        }

        @Test
        @DisplayName("历史未超过 10 条时不触发 lTrim")
        void doesNotTrimWhenHistoryWithinLimit() {
            when(redisUtils.lLen(anyString())).thenReturn(2L);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(redisUtils, never()).lTrim(anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("请求体中 model 字段与配置一致")
        void requestBodyContainsCorrectModel() {
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));

            @SuppressWarnings("unchecked")
            HttpEntity<Map<String, Object>> entity = (HttpEntity<Map<String, Object>>) captor.getValue();
            assertThat(entity.getBody().get("model")).isEqualTo("qwen-turbo");
        }
    }

    // ------------------------------------------------------------------
    //  chat() —— 异常情况
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("chat() 异常情况")
    class ChatExceptions {

        @Test
        @DisplayName("API Key 为空时抛出 BusinessException 且不调用远程接口")
        void throwsWhenApiKeyBlank() {
            ReflectionTestUtils.setField(aiService, "apiKey", "");
            assertThatThrownBy(() -> aiService.chat(1L, buildDto("问题", "GENERAL", null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("AI服务未配置");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("用户不存在时抛出 BusinessException 且不调用远程接口")
        void throwsWhenUserNotFound() {
            when(userMapper.selectById(1L)).thenReturn(null);
            assertThatThrownBy(() -> aiService.chat(1L, buildDto("问题", "GENERAL", null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("DashScope 接口抛出网络异常时包装为 BusinessException")
        void throwsWhenApiCallFails() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            when(redisUtils.lRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptyList());
            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenThrow(new RestClientException("连接超时"));

            assertThatThrownBy(() -> aiService.chat(1L, buildDto("问题", "GENERAL", null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("AI服务暂时不可用");
        }
    }

    // ------------------------------------------------------------------
    //  system prompt 构造 —— 年龄段和上下文
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("buildSystemPrompt() 年龄段与上下文")
    class SystemPromptBuilding {

        @BeforeEach
        void commonStubs() {
            when(redisUtils.lRange(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptyList());
            when(redisUtils.lLen(anyString())).thenReturn(2L);
            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenReturn(mockApiResponse("好的"));
        }

        @Test
        @DisplayName("年龄段 1（幼儿园）prompt 包含幼儿园描述")
        void ageGroup1ContainsKindergartenHint() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(1, 1));
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
            assertThat(captureMessages(captor).get(0).get("content")).contains("幼儿园");
        }

        @Test
        @DisplayName("年龄段 2（小学）prompt 包含小学生描述")
        void ageGroup2ContainsPrimaryHint() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
            assertThat(captureMessages(captor).get(0).get("content")).contains("小学生");
        }

        @Test
        @DisplayName("年龄段 3（初中）prompt 包含初中生描述")
        void ageGroup3ContainsJuniorHint() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(3, 5));
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
            assertThat(captureMessages(captor).get(0).get("content")).contains("初中生");
        }

        @Test
        @DisplayName("GAME_HINT 且关卡存在时 prompt 包含关卡标题和知识点")
        void gameHintAppendsLevelInfo() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            GameContent gc = new GameContent();
            gc.setTitle("猎户座");
            gc.setKnowledgePoint("冬季最易观测的星座");
            when(gameContentMapper.selectById(10L)).thenReturn(gc);
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

            aiService.chat(1L, buildDto("给我提示", "GAME_HINT", 10L));

            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
            String prompt = captureMessages(captor).get(0).get("content");
            assertThat(prompt).contains("猎户座").contains("冬季最易观测的星座");
        }

        @Test
        @DisplayName("GAME_HINT 但关卡不存在时正常返回不抛异常")
        void gameHintWithMissingLevelDoesNotThrow() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            when(gameContentMapper.selectById(99L)).thenReturn(null);
            assertThatCode(() -> aiService.chat(1L, buildDto("提示", "GAME_HINT", 99L)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("prompt 中包含用户等级信息")
        void promptContainsUserLevel() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 7));
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            aiService.chat(1L, buildDto("问题", "GENERAL", null));
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
            assertThat(captureMessages(captor).get(0).get("content")).contains("7级");
        }
    }

    // ------------------------------------------------------------------
    //  多轮对话历史
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("多轮对话历史")
    class ConversationHistory {

        @Test
        @DisplayName("Redis 有历史时将历史消息插入 system 和 user 消息之间")
        void includesHistoryFromRedis() throws Exception {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            String histMsg = objectMapper.writeValueAsString(Map.of("role", "user", "content", "上一个问题"));
            when(redisUtils.lRange(eq("ai:session:1"), eq(0L), eq(-1L))).thenReturn(List.of(histMsg));
            when(redisUtils.lLen(anyString())).thenReturn(4L);
            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenReturn(mockApiResponse("回复"));

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            aiService.chat(1L, buildDto("新问题", "GENERAL", null));
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));

            List<Map<String, String>> messages = captureMessages(captor);
            // system(0) + history(1) + current(2) = 3 条
            assertThat(messages).hasSize(3);
            assertThat(messages.get(0).get("role")).isEqualTo("system");
            assertThat(messages.get(1).get("content")).isEqualTo("上一个问题");
            assertThat(messages.get(2).get("content")).isEqualTo("新问题");
        }

        @Test
        @DisplayName("无效 JSON 历史被跳过，请求正常发出")
        void skipsInvalidHistoryJson() {
            when(userMapper.selectById(1L)).thenReturn(buildUser(2, 3));
            when(redisUtils.lRange(anyString(), anyLong(), anyLong())).thenReturn(List.of("{bad-json}"));
            when(redisUtils.lLen(anyString())).thenReturn(2L);
            when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .thenReturn(mockApiResponse("回复"));

            assertThatCode(() -> aiService.chat(1L, buildDto("问题", "GENERAL", null)))
                    .doesNotThrowAnyException();
            // 只有 system + current，跳过了无效历史
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
            assertThat(captureMessages(captor)).hasSize(2);
        }
    }

    // ------------------------------------------------------------------
    //  clearSession()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("clearSession() 删除对应用户的 Redis 会话 key")
    void clearSessionDeletesRedisKey() {
        aiService.clearSession(1L);
        verify(redisUtils).delete("ai:session:1");
    }

    @Test
    @DisplayName("clearSession() 不操作其他用户的 key")
    void clearSessionOnlyDeletesTargetKey() {
        aiService.clearSession(42L);
        verify(redisUtils).delete("ai:session:42");
        verify(redisUtils, never()).delete("ai:session:1");
    }
}
