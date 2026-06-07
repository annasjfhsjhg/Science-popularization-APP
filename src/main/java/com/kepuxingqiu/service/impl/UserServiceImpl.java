package com.kepuxingqiu.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kepuxingqiu.common.exception.BusinessException;
import com.kepuxingqiu.common.result.ResultCode;
import com.kepuxingqiu.common.utils.JwtUtils;
import com.kepuxingqiu.common.utils.RedisUtils;
import com.kepuxingqiu.dto.LoginDTO;
import com.kepuxingqiu.dto.RegisterDTO;
import com.kepuxingqiu.dto.UpdateProfileDTO;
import com.kepuxingqiu.entity.User;
import com.kepuxingqiu.mapper.UserAchievementMapper;
import com.kepuxingqiu.mapper.UserEncyclopediaMapper;
import com.kepuxingqiu.mapper.UserGameProgressMapper;
import com.kepuxingqiu.mapper.UserMapper;
import com.kepuxingqiu.service.UserService;
import com.kepuxingqiu.vo.LoginVO;
import com.kepuxingqiu.vo.UserInfoVO;
import com.kepuxingqiu.vo.UserProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final UserGameProgressMapper progressMapper;
    private final UserEncyclopediaMapper encMapper;
    private final UserAchievementMapper achievementMapper;

    public UserServiceImpl(UserMapper userMapper, JwtUtils jwtUtils, RedisUtils redisUtils,
                           UserGameProgressMapper progressMapper, UserEncyclopediaMapper encMapper,
                           UserAchievementMapper achievementMapper) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
        this.progressMapper = progressMapper;
        this.encMapper = encMapper;
        this.achievementMapper = achievementMapper;
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_USERNAME);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setAgeGroup(dto.getAgeGroup());
        user.setLevel(1);
        user.setExperience(0);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId());
        redisUtils.set("token:" + user.getId(), token, jwtUtils.getExpireSeconds(), TimeUnit.SECONDS);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setLevel(user.getLevel());
        vo.setExperience(user.getExperience());
        return vo;
    }

    @Override
    public void logout(Long userId) {
        redisUtils.delete("token:" + userId);
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // cache-aside：优先读 Redis，缺失时回源数据库并写入缓存
        String cacheKey = "user:level:" + userId;
        String cachedLevel = redisUtils.hget(cacheKey, "level");
        String cachedExp   = redisUtils.hget(cacheKey, "exp");

        int level;
        int exp;
        if (cachedLevel != null && cachedExp != null) {
            level = Integer.parseInt(cachedLevel);
            exp   = Integer.parseInt(cachedExp);
        } else {
            // 缓存缺失：写入缓存，等待 Canal 后续保持同步
            level = user.getLevel();
            exp   = user.getExperience();
            redisUtils.hset(cacheKey, "level", String.valueOf(level));
            redisUtils.hset(cacheKey, "exp",   String.valueOf(exp));
            redisUtils.expire(cacheKey, 3600, TimeUnit.SECONDS);
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setLevel(level);
        vo.setExp(exp);
        vo.setNextLevelExp(level * 200);
        long days = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now());
        vo.setJoinDays((int) days + 1);
        String avatar = user.getAvatarUrl();
        vo.setAvatar(avatar != null && !avatar.isEmpty() ? avatar : "🧒");
        return vo;
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return buildProfileVO(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User update = new User();
        update.setId(userId);
        if (dto.getNickname() != null) update.setNickname(dto.getNickname());
        if (dto.getAvatarUrl() != null) update.setAvatarUrl(dto.getAvatarUrl());
        userMapper.updateById(update);
    }

    @Override
    public UserProfileVO getLearningReport(Long userId) {
        return getProfile(userId);
    }

    private UserProfileVO buildProfileVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAgeGroup(user.getAgeGroup());
        vo.setLevel(user.getLevel());
        vo.setExperience(user.getExperience());
        vo.setNextLevelExp(user.getLevel() * 200);
        vo.setUnlockedEncCount(encMapper.countUnlocked(user.getId()));
        vo.setCompletedGameCount(progressMapper.countTotalCompleted(user.getId()));
        vo.setAchievementCount(Math.toIntExact(
                achievementMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.kepuxingqiu.entity.UserAchievement>()
                                .eq(com.kepuxingqiu.entity.UserAchievement::getUserId, user.getId())
                                .eq(com.kepuxingqiu.entity.UserAchievement::getIsCompleted, 1))));
        return vo;
    }
}
