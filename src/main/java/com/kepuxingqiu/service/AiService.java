package com.kepuxingqiu.service;

import com.kepuxingqiu.dto.AiChatDTO;
import com.kepuxingqiu.vo.AiChatVO;

public interface AiService {
    AiChatVO chat(Long userId, AiChatDTO dto);
    void clearSession(Long userId);
}
