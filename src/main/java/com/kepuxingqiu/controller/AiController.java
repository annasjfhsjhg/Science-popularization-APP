package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.dto.AiChatDTO;
import com.kepuxingqiu.service.AiService;
import com.kepuxingqiu.vo.AiChatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI小助手", description = "基于通义千问的科普对话助手")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "发送消息给AI助手（支持多轮对话）")
    @PostMapping("/chat")
    public Result<AiChatVO> chat(@Valid @RequestBody AiChatDTO dto) {
        return Result.success(aiService.chat(UserContext.getUserId(), dto));
    }

    @Operation(summary = "清除当前会话历史")
    @DeleteMapping("/session")
    public Result<Void> clearSession() {
        aiService.clearSession(UserContext.getUserId());
        return Result.success(null);
    }
}
