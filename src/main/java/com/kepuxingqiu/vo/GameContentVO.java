package com.kepuxingqiu.vo;

import lombok.Data;

@Data
public class GameContentVO {
    private Long id;
    private Integer categoryId;
    private String title;
    private Integer difficulty;
    /** 用户在该关卡的状态: 0=未开始 1=进行中 2=已完成 */
    private Integer userStatus;
    private Integer userScore;
    private Integer experienceReward;
    /** 详情接口才返回 */
    private String contentData;
    private String knowledgePoint;
}
