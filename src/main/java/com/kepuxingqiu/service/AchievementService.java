package com.kepuxingqiu.service;

import com.kepuxingqiu.vo.AchievementVO;

import java.util.List;

public interface AchievementService {
    List<AchievementVO> list(Long userId);
    AchievementVO getDetail(Long userId, Integer achievementId);
}
