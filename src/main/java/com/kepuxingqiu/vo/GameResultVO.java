package com.kepuxingqiu.vo;

import lombok.Data;

import java.util.List;

@Data
public class GameResultVO {
    private Integer expGained;
    /** 本次解锁的图鉴名称列表 */
    private List<String> unlocked;
}
