package com.kepuxingqiu.vo;

import lombok.Data;

import java.util.List;

@Data
public class GameSubmitResultVO {
    private Integer earnedExp;
    private Boolean levelUp;
    private Integer newLevel;
    /** 新解锁的图鉴列表 */
    private List<EncyclopediaVO> newUnlockedEnc;
    /** 新达成的成就列表 */
    private List<AchievementVO> newAchievements;
}
