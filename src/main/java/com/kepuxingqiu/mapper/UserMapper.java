package com.kepuxingqiu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kepuxingqiu.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
