package com.kepuxingqiu.service;

import com.kepuxingqiu.vo.AchievementVO;
import com.kepuxingqiu.vo.EncyclopediaVO;
import com.kepuxingqiu.vo.GameSubmitResultVO;

public interface ExperienceService {
    /** 游戏通关后统一处理：经验、升级、图鉴解锁、成就 */
    GameSubmitResultVO processGameCompletion(Long userId, Long gameContentId, Integer earnedExp);
}
