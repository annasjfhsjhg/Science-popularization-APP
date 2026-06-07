package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("game_content")
public class GameContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer categoryId;
    private String title;
    /** 难度: 1=简单 2=中等 3=困难 */
    private Integer difficulty;
    /** JSON字符串，存储游戏配置（星座点位、拼图数据等） */
    private String contentData;
    private String knowledgePoint;
    private Integer experienceReward;
    private Integer sortOrder;
}
