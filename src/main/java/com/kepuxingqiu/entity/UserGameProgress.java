package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_game_progress")
public class UserGameProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long gameContentId;
    /** 状态: 0=未开始 1=进行中 2=已完成 */
    private Integer status;
    private Integer score;
    private LocalDateTime completedAt;
}
