package com.kepuxingqiu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    /** 年龄段: 1=幼儿园 2=小学 3=初中 */
    private Integer ageGroup;
    private Integer level;
    private Integer experience;
    private LocalDateTime createdAt;
}
