package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_achievement")
public class UserAchievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer achievementId;
    private Integer currentProgress;
    /** 是否完成: 0=未完成 1=已完成 */
    private Integer isCompleted;
    private LocalDateTime completedAt;
}
