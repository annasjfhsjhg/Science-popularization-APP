package com.kepuxingqiu.vo;

import lombok.Data;

@Data
public class EncyclopediaVO {
    private Long id;
    private String category;
    private String name;
    private String description;
    private String imageUrl;
    private String unlockCondition;
    /** 是否已解锁 */
    private Boolean unlocked;
}
