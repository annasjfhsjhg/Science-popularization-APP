package com.kepuxingqiu.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long userId;
    private String nickname;
    private Integer level;
    private Integer exp;
    private Integer nextLevelExp;
    private Integer joinDays;
    private String avatar;
}
