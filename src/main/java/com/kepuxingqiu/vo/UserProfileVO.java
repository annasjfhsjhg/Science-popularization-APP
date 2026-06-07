package com.kepuxingqiu.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer ageGroup;
    private Integer level;
    private Integer experience;
    /** 升级所需经验 = level * 200 */
    private Integer nextLevelExp;
    /** 统计数据 */
    private Integer unlockedEncCount;
    private Integer completedGameCount;
    private Integer achievementCount;
}
