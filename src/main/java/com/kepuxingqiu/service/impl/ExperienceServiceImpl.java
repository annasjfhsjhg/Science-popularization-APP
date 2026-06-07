package com.kepuxingqiu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kepuxingqiu.entity.*;
import com.kepuxingqiu.mapper.*;
import com.kepuxingqiu.service.ExperienceService;
import com.kepuxingqiu.vo.AchievementVO;
import com.kepuxingqiu.vo.EncyclopediaVO;
import com.kepuxingqiu.vo.GameSubmitResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExperienceServiceImpl implements ExperienceService {

    private final UserMapper userMapper;
    private final EncyclopediaMapper encyclopediaMapper;
    private final UserEncyclopediaMapper userEncMapper;
    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserGameProgressMapper progressMapper;
    private final GameContentMapper gameContentMapper;

    public ExperienceServiceImpl(UserMapper userMapper, EncyclopediaMapper encyclopediaMapper,
                                 UserEncyclopediaMapper userEncMapper, AchievementMapper achievementMapper,
                                 UserAchievementMapper userAchievementMapper, UserGameProgressMapper progressMapper,
                                 GameContentMapper gameContentMapper) {
        this.userMapper = userMapper;
        this.encyclopediaMapper = encyclopediaMapper;
        this.userEncMapper = userEncMapper;
        this.achievementMapper = achievementMapper;
        this.userAchievementMapper = userAchievementMapper;
        this.progressMapper = progressMapper;
        this.gameContentMapper = gameContentMapper;
    }

    @Override
    @Transactional
    public GameSubmitResultVO processGameCompletion(Long userId, Long gameContentId, Integer earnedExp) {
        GameSubmitResultVO result = new GameSubmitResultVO();
        result.setEarnedExp(earnedExp);
        result.setLevelUp(false);

        // 1. 更新经验，检查升级
        User user = userMapper.selectById(userId);
        int newExp = user.getExperience() + earnedExp;
        int newLevel = user.getLevel();
        boolean levelUp = false;

        // 升级阈值：当前等级 * 200
        while (newExp >= newLevel * 200) {
            newExp -= newLevel * 200;
            newLevel++;
            levelUp = true;
        }

        User update = new User();
        update.setId(userId);
        update.setExperience(newExp);
        update.setLevel(newLevel);
        userMapper.updateById(update);

        result.setLevelUp(levelUp);
        result.setNewLevel(newLevel);

        // 2. 解锁关联图鉴
        List<EncyclopediaVO> newUnlocked = tryUnlockEncyclopedia(userId, gameContentId);
        result.setNewUnlockedEnc(newUnlocked);

        // 3. 检查并更新成就
        List<AchievementVO> newAchievements = checkAndUpdateAchievements(userId, newLevel, gameContentId);
        result.setNewAchievements(newAchievements);

        // 4. 成就奖励经验补充发放
        int bonusExp = newAchievements.stream()
                .mapToInt(a -> {
                    Achievement ach = achievementMapper.selectById(a.getId());
                    return ach != null ? ach.getExperienceReward() : 0;
                }).sum();
        if (bonusExp > 0) {
            User bonusUpdate = new User();
            bonusUpdate.setId(userId);
            bonusUpdate.setExperience(newExp + bonusExp);
            userMapper.updateById(bonusUpdate);
        }

        return result;
    }

    private List<EncyclopediaVO> tryUnlockEncyclopedia(Long userId, Long gameContentId) {
        List<EncyclopediaVO> unlocked = new ArrayList<>();
        List<Encyclopedia> encList = encyclopediaMapper.selectList(
                new LambdaQueryWrapper<Encyclopedia>().eq(Encyclopedia::getRelatedGameId, gameContentId));
        for (Encyclopedia enc : encList) {
            Long already = userEncMapper.selectCount(
                    new LambdaQueryWrapper<UserEncyclopedia>()
                            .eq(UserEncyclopedia::getUserId, userId)
                            .eq(UserEncyclopedia::getEncyclopediaId, enc.getId()));
            if (already == 0) {
                UserEncyclopedia ue = new UserEncyclopedia();
                ue.setUserId(userId);
                ue.setEncyclopediaId(enc.getId());
                ue.setUnlockedAt(LocalDateTime.now());
                userEncMapper.insert(ue);

                EncyclopediaVO vo = new EncyclopediaVO();
                vo.setId(enc.getId());
                vo.setName(enc.getName());
                vo.setCategory(enc.getCategory());
                vo.setImageUrl(enc.getImageUrl());
                vo.setUnlocked(true);
                unlocked.add(vo);
            }
        }
        return unlocked;
    }

    private List<AchievementVO> checkAndUpdateAchievements(Long userId, int currentLevel, Long gameContentId) {
        List<AchievementVO> newCompleted = new ArrayList<>();
        List<Achievement> allAchievements = achievementMapper.selectList(null);

        for (Achievement ach : allAchievements) {
            UserAchievement ua = userAchievementMapper.selectOne(
                    new LambdaQueryWrapper<UserAchievement>()
                            .eq(UserAchievement::getUserId, userId)
                            .eq(UserAchievement::getAchievementId, ach.getId()));

            if (ua != null && ua.getIsCompleted() == 1) continue;

            int progress = computeProgress(userId, ach, currentLevel, gameContentId);
            boolean justCompleted = progress >= ach.getConditionValue();

            if (ua == null) {
                ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(ach.getId());
                ua.setCurrentProgress(progress);
                ua.setIsCompleted(justCompleted ? 1 : 0);
                if (justCompleted) ua.setCompletedAt(LocalDateTime.now());
                userAchievementMapper.insert(ua);
            } else {
                ua.setCurrentProgress(progress);
                if (justCompleted) {
                    ua.setIsCompleted(1);
                    ua.setCompletedAt(LocalDateTime.now());
                }
                userAchievementMapper.updateById(ua);
            }

            if (justCompleted && (ua.getIsCompleted() == null || ua.getIsCompleted() == 0)) {
                AchievementVO vo = new AchievementVO();
                vo.setId(ach.getId());
                vo.setName(ach.getName());
                vo.setDescription(ach.getDescription());
                vo.setIcon(ach.getIcon());
                vo.setUnlocked(true);
                newCompleted.add(vo);
            }
        }
        return newCompleted;
    }

    private int computeProgress(Long userId, Achievement ach, int currentLevel, Long gameContentId) {
        return switch (ach.getConditionType()) {
            case "complete_game" -> progressMapper.countTotalCompleted(userId);
            case "unlock_enc" -> userEncMapper.countUnlocked(userId);
            case "reach_level" -> currentLevel;
            case "complete_game_category" -> {
                GameContent gc = gameContentMapper.selectById(gameContentId);
                yield gc != null ? progressMapper.countCompletedByCategoryId(userId, gc.getCategoryId()) : 0;
            }
            case "unlock_enc_category" -> {
                GameContent gc = gameContentMapper.selectById(gameContentId);
                if (gc == null) yield 0;
                // 根据分类ID映射到图鉴category字符串
                String cat = switch (gc.getCategoryId()) {
                    case 1 -> "astronomy";
                    case 2 -> "history";
                    case 3 -> "insect";
                    default -> "";
                };
                yield userEncMapper.countUnlockedByCategory(userId, cat);
            }
            default -> 0;
        };
    }
}
