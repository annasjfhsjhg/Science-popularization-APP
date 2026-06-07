package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("game_category")
public class GameCategory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
}
