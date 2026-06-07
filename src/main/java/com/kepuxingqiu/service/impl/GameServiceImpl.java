package com.kepuxingqiu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepuxingqiu.common.exception.BusinessException;
import com.kepuxingqiu.common.result.ResultCode;
import com.kepuxingqiu.dto.GameResultDTO;
import com.kepuxingqiu.dto.GameSubmitDTO;
import com.kepuxingqiu.entity.*;
import com.kepuxingqiu.mapper.*;
import com.kepuxingqiu.service.ExperienceService;
import com.kepuxingqiu.service.GameService;
import com.kepuxingqiu.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private final GameCategoryMapper categoryMapper;
    private final GameContentMapper contentMapper;
    private final UserGameProgressMapper progressMapper;
    private final UserMapper userMapper;
    private final ExperienceService experienceService;
    private final UserEncyclopediaMapper encMapper;

    public GameServiceImpl(GameCategoryMapper categoryMapper, GameContentMapper contentMapper,
                           UserGameProgressMapper progressMapper, UserMapper userMapper,
                           ExperienceService experienceService, UserEncyclopediaMapper encMapper) {
        this.categoryMapper = categoryMapper;
        this.contentMapper = contentMapper;
        this.progressMapper = progressMapper;
        this.userMapper = userMapper;
        this.experienceService = experienceService;
        this.encMapper = encMapper;
    }

    @Override
    public HomeVO getHomeDashboard(Long userId) {
        User user = userMapper.selectById(userId);
        HomeVO vo = new HomeVO();
        vo.setNickname(user.getNickname());
        vo.setLevel(user.getLevel());
        vo.setExperience(user.getExperience());
        int nextExp = user.getLevel() * 200;
        vo.setNextLevelExp(nextExp);
        vo.setExpPercent(Math.min(100, user.getExperience() * 100 / nextExp));

        List<GameCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<GameCategory>().orderByAsc(GameCategory::getSortOrder));
        List<HomeVO.CategoryVO> categoryVOs = new ArrayList<>();
        for (GameCategory cat : categories) {
            HomeVO.CategoryVO cvo = new HomeVO.CategoryVO();
            cvo.setId(cat.getId());
            cvo.setName(cat.getName());
            cvo.setDescription(cat.getDescription());
            cvo.setIcon(cat.getIcon());
            long total = contentMapper.selectCount(
                    new LambdaQueryWrapper<GameContent>().eq(GameContent::getCategoryId, cat.getId()));
            cvo.setTotalCount((int) total);
            cvo.setCompletedCount(progressMapper.countCompletedByCategoryId(userId, cat.getId()));
            categoryVOs.add(cvo);
        }
        vo.setCategories(categoryVOs);
        return vo;
    }

    @Override
    public List<GameContentVO> listByCategory(Long userId, Integer categoryId) {
        List<GameContent> contents = contentMapper.selectList(
                new LambdaQueryWrapper<GameContent>()
                        .eq(GameContent::getCategoryId, categoryId)
                        .orderByAsc(GameContent::getSortOrder));

        List<Long> ids = contents.stream().map(GameContent::getId).collect(Collectors.toList());
        Map<Long, UserGameProgress> progressMap = progressMapper.selectList(
                new LambdaQueryWrapper<UserGameProgress>()
                        .eq(UserGameProgress::getUserId, userId)
                        .in(!ids.isEmpty(), UserGameProgress::getGameContentId, ids))
                .stream().collect(Collectors.toMap(UserGameProgress::getGameContentId, p -> p));

        return contents.stream().map(c -> {
            GameContentVO vo = new GameContentVO();
            vo.setId(c.getId());
            vo.setCategoryId(c.getCategoryId());
            vo.setTitle(c.getTitle());
            vo.setDifficulty(c.getDifficulty());
            vo.setExperienceReward(c.getExperienceReward());
            UserGameProgress p = progressMap.get(c.getId());
            vo.setUserStatus(p != null ? p.getStatus() : 0);
            vo.setUserScore(p != null ? p.getScore() : 0);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public GameContentVO getDetail(Long userId, Long contentId) {
        GameContent content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关卡不存在");
        }
        GameContentVO vo = new GameContentVO();
        vo.setId(content.getId());
        vo.setCategoryId(content.getCategoryId());
        vo.setTitle(content.getTitle());
        vo.setDifficulty(content.getDifficulty());
        vo.setExperienceReward(content.getExperienceReward());
        vo.setContentData(content.getContentData());
        vo.setKnowledgePoint(content.getKnowledgePoint());

        UserGameProgress p = progressMapper.selectOne(
                new LambdaQueryWrapper<UserGameProgress>()
                        .eq(UserGameProgress::getUserId, userId)
                        .eq(UserGameProgress::getGameContentId, contentId));
        vo.setUserStatus(p != null ? p.getStatus() : 0);
        vo.setUserScore(p != null ? p.getScore() : 0);
        return vo;
    }

    @Override
    @Transactional
    public GameSubmitResultVO submitGame(Long userId, GameSubmitDTO dto) {
        GameContent content = contentMapper.selectById(dto.getGameContentId());
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关卡不存在");
        }

        UserGameProgress progress = progressMapper.selectOne(
                new LambdaQueryWrapper<UserGameProgress>()
                        .eq(UserGameProgress::getUserId, userId)
                        .eq(UserGameProgress::getGameContentId, dto.getGameContentId()));

        boolean isFirstComplete = (progress == null || progress.getStatus() != 2) && Boolean.TRUE.equals(dto.getCompleted());

        if (progress == null) {
            progress = new UserGameProgress();
            progress.setUserId(userId);
            progress.setGameContentId(dto.getGameContentId());
            progress.setStatus(Boolean.TRUE.equals(dto.getCompleted()) ? 2 : 1);
            progress.setScore(dto.getScore() != null ? dto.getScore() : 0);
            if (Boolean.TRUE.equals(dto.getCompleted())) progress.setCompletedAt(LocalDateTime.now());
            progressMapper.insert(progress);
        } else {
            // 只更新更高分
            if (dto.getScore() != null && dto.getScore() > progress.getScore()) {
                progress.setScore(dto.getScore());
            }
            if (Boolean.TRUE.equals(dto.getCompleted()) && progress.getStatus() != 2) {
                progress.setStatus(2);
                progress.setCompletedAt(LocalDateTime.now());
            }
            progressMapper.updateById(progress);
        }

        // 首次通关才给经验奖励
        if (isFirstComplete) {
            return experienceService.processGameCompletion(userId, dto.getGameContentId(), content.getExperienceReward());
        }

        GameSubmitResultVO vo = new GameSubmitResultVO();
        vo.setEarnedExp(0);
        vo.setLevelUp(false);
        vo.setNewUnlockedEnc(List.of());
        vo.setNewAchievements(List.of());
        return vo;
    }

    @Override
    public GameLevelVO getLevel(String gameType, Long userId) {
        Integer categoryId = gameTypeToCategoryId(gameType);
        GameContent content = contentMapper.selectOne(
                new LambdaQueryWrapper<GameContent>()
                        .eq(GameContent::getCategoryId, categoryId)
                        .orderByAsc(GameContent::getSortOrder)
                        .last("LIMIT 1"));
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "暂无该类型关卡");
        }
        return buildGameLevelVO(content, gameType);
    }

    @Override
    @Transactional
    public GameResultVO submitGameResult(Long userId, GameResultDTO dto) {
        Integer categoryId = gameTypeToCategoryId(dto.getGameType());
        GameContent content = contentMapper.selectOne(
                new LambdaQueryWrapper<GameContent>()
                        .eq(GameContent::getCategoryId, categoryId)
                        .orderByAsc(GameContent::getSortOrder)
                        .last("LIMIT 1"));
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "暂无该类型关卡");
        }

        UserGameProgress progress = progressMapper.selectOne(
                new LambdaQueryWrapper<UserGameProgress>()
                        .eq(UserGameProgress::getUserId, userId)
                        .eq(UserGameProgress::getGameContentId, content.getId()));

        boolean isFirstComplete = progress == null || progress.getStatus() != 2;

        if (progress == null) {
            progress = new UserGameProgress();
            progress.setUserId(userId);
            progress.setGameContentId(content.getId());
            progress.setStatus(2);
            progress.setScore(dto.getScore() != null ? dto.getScore() : 0);
            progress.setCompletedAt(LocalDateTime.now());
            progressMapper.insert(progress);
        } else if (progress.getStatus() != 2) {
            progress.setStatus(2);
            if (dto.getScore() != null && dto.getScore() > progress.getScore()) {
                progress.setScore(dto.getScore());
            }
            progress.setCompletedAt(LocalDateTime.now());
            progressMapper.updateById(progress);
        }

        GameResultVO vo = new GameResultVO();
        if (isFirstComplete) {
            int starsRating = dto.getStars() != null ? dto.getStars() : 3;
            int earnedExp = Math.round(content.getExperienceReward() * starsRating / 3f);
            GameSubmitResultVO result = experienceService.processGameCompletion(userId, content.getId(), earnedExp);
            vo.setExpGained(result.getEarnedExp());
            vo.setUnlocked(result.getNewUnlockedEnc() != null
                    ? result.getNewUnlockedEnc().stream().map(EncyclopediaVO::getName).collect(Collectors.toList())
                    : List.of());
        } else {
            vo.setExpGained(0);
            vo.setUnlocked(List.of());
        }
        return vo;
    }

    private Integer gameTypeToCategoryId(String gameType) {
        return switch (gameType) {
            case "astronomy" -> 1;
            case "history" -> 2;
            case "insect" -> 3;
            default -> throw new BusinessException(ResultCode.PARAM_ERROR, "无效的gameType: " + gameType);
        };
    }

    private GameLevelVO buildGameLevelVO(GameContent content, String gameType) {
        GameLevelVO vo = new GameLevelVO();
        vo.setGameId(content.getId() + "_" + gameType);
        vo.setGameType(gameType);

        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode node = om.readTree(content.getContentData());
            List<GameLevelVO.StarPoint> starPoints = new ArrayList<>();
            JsonNode starsNode = node.get("stars");
            if (starsNode != null && starsNode.isArray()) {
                for (JsonNode star : starsNode) {
                    GameLevelVO.StarPoint sp = new GameLevelVO.StarPoint();
                    sp.setId(star.get("id").asInt());
                    sp.setX(star.get("x").asInt());
                    sp.setY(star.get("y").asInt());
                    starPoints.add(sp);
                }
            }
            vo.setStars(starPoints);
            vo.setCorrectOrder(starPoints.stream().map(GameLevelVO.StarPoint::getId).collect(Collectors.toList()));
        } catch (Exception e) {
            vo.setStars(List.of());
            vo.setCorrectOrder(List.of());
        }
        return vo;
    }
}
