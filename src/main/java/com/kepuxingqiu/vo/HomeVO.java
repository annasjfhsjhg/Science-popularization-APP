package com.kepuxingqiu.vo;

import lombok.Data;

import java.util.List;

@Data
public class HomeVO {
    /** 用户等级卡信息 */
    private String nickname;
    private Integer level;
    private Integer experience;
    private Integer nextLevelExp;
    /** 经验进度百分比 0-100 */
    private Integer expPercent;

    /** 游戏分类列表 */
    private List<CategoryVO> categories;

    /** 每日推荐（取一个随机图鉴） */
    private EncyclopediaVO dailyRecommend;

    @Data
    public static class CategoryVO {
        private Integer id;
        private String name;
        private String description;
        private String icon;
        /** 该分类下用户已完成关卡数 */
        private Integer completedCount;
        /** 该分类总关卡数 */
        private Integer totalCount;
    }
}
