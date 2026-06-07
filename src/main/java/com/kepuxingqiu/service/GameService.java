package com.kepuxingqiu.service;

import com.kepuxingqiu.dto.GameResultDTO;
import com.kepuxingqiu.dto.GameSubmitDTO;
import com.kepuxingqiu.vo.*;

import java.util.List;

public interface GameService {
    HomeVO getHomeDashboard(Long userId);
    List<GameContentVO> listByCategory(Long userId, Integer categoryId);
    GameContentVO getDetail(Long userId, Long contentId);
    GameSubmitResultVO submitGame(Long userId, GameSubmitDTO dto);
    GameLevelVO getLevel(String gameType, Long userId);
    GameResultVO submitGameResult(Long userId, GameResultDTO dto);
}
