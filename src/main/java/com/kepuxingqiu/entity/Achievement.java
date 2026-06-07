package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private String icon;
    /** 条件类型: complete_game / unlock_enc / reach_level / complete_game_category */
    private String conditionType;
    private Integer conditionValue;
    private Integer experienceReward;
}
