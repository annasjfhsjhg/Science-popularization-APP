package com.kepuxingqiu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kepuxingqiu.entity.UserGameProgress;
import com.kepuxingqiu.vo.GameProgressVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserGameProgressMapper extends BaseMapper<UserGameProgress> {

    /** 查询用户在某分类下已完成的关卡数 */
    int countCompletedByCategoryId(@Param("userId") Long userId, @Param("categoryId") Integer categoryId);

    /** 查询用户总完成关卡数 */
    int countTotalCompleted(@Param("userId") Long userId);

    /** 查询用户在各分类的进度摘要，用于学习报告 */
    List<GameProgressVO> selectProgressSummary(@Param("userId") Long userId);
}
