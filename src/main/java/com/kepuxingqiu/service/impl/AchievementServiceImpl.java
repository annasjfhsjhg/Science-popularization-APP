package com.kepuxingqiu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kepuxingqiu.common.exception.BusinessException;
import com.kepuxingqiu.common.result.ResultCode;
import com.kepuxingqiu.entity.Achievement;
import com.kepuxingqiu.entity.UserAchievement;
import com.kepuxingqiu.mapper.AchievementMapper;
import com.kepuxingqiu.mapper.UserAchievementMapper;
import com.kepuxingqiu.service.AchievementService;
import com.kepuxingqiu.vo.AchievementVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    public AchievementServiceImpl(AchievementMapper achievementMapper, UserAchievementMapper userAchievementMapper) {
        this.achievementMapper = achievementMapper;
        this.userAchievementMapper = userAchievementMapper;
    }

    @Override
    public List<AchievementVO> list(Long userId) {
        List<Achievement> achievements = achievementMapper.selectList(null);
        Map<Integer, UserAchievement> userMap = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId))
                .stream().collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        return achievements.stream().map(ach -> toVO(ach, userMap.get(ach.getId()))).collect(Collectors.toList());
    }

    @Override
    public AchievementVO getDetail(Long userId, Integer achievementId) {
        Achievement ach = achievementMapper.selectById(achievementId);
        if (ach == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成就不存在");
        }
        UserAchievement ua = userAchievementMapper.selectOne(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
                        .eq(UserAchievement::getAchievementId, achievementId));
        return toVO(ach, ua);
    }

    private AchievementVO toVO(Achievement ach, UserAchievement ua) {
        AchievementVO vo = new AchievementVO();
        vo.setId(ach.getId());
        vo.setName(ach.getName());
        vo.setDescription(ach.getDescription());
        vo.setIcon(ach.getIcon());
        vo.setExperienceReward(ach.getExperienceReward());

        int progress = ua != null ? ua.getCurrentProgress() : 0;
        vo.setProgress(progress);
        vo.setTotal(ach.getConditionValue());
        vo.setUnlocked(ua != null && ua.getIsCompleted() == 1);
        return vo;
    }
}
