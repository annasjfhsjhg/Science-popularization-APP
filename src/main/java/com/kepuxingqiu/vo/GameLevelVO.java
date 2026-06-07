package com.kepuxingqiu.vo;

import lombok.Data;

import java.util.List;

@Data
public class GameLevelVO {
    private String gameId;
    private String gameType;
    private List<StarPoint> stars;
    private List<Integer> correctOrder;

    @Data
    public static class StarPoint {
        private Integer id;
        private Integer x;
        private Integer y;
    }
}
