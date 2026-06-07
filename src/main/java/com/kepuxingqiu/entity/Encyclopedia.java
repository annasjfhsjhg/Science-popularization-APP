package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("encyclopedia")
public class Encyclopedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类: astronomy / history / insect */
    private String category;
    private String name;
    private String description;
    private String imageUrl;
    private String unlockCondition;
    private Long relatedGameId;
}
