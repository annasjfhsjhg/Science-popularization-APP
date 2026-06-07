package com.kepuxingqiu.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String nickname;
    private Integer level;
    private Integer experience;
}
