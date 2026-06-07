package com.kepuxingqiu.vo;

import lombok.Data;

@Data
public class AchievementVO {
    private Integer id;
    private String name;
    private String description;
    private String icon;
    private Integer experienceReward;
    /** 当前进度（对应前端 progress 字段） */
    private Integer progress;
    /** 目标总量（对应前端 total 字段） */
    private Integer total;
    /** 是否已解锁/完成（对应前端 unlocked 字段） */
    private Boolean unlocked;
}
