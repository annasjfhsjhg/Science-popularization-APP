package com.kepuxingqiu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kepuxingqiu.entity.UserEncyclopedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserEncyclopediaMapper extends BaseMapper<UserEncyclopedia> {

    /** 统计用户解锁的图鉴总数 */
    int countUnlocked(@Param("userId") Long userId);

    /** 统计用户在某分类下解锁的图鉴数 */
    int countUnlockedByCategory(@Param("userId") Long userId, @Param("category") String category);
}
