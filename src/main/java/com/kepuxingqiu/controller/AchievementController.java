package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.service.AchievementService;
import com.kepuxingqiu.vo.AchievementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "成就模块", description = "成就列表与进度")
@RestController
@RequestMapping("/api/achievement")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @Operation(summary = "获取成就列表（含用户进度）")
    @GetMapping("/list")
    public Result<List<AchievementVO>> list() {
        return Result.success(achievementService.list(UserContext.getUserId()));
    }

    @Operation(summary = "获取成就详情")
    @GetMapping("/detail/{id}")
    public Result<AchievementVO> getDetail(@PathVariable Integer id) {
        return Result.success(achievementService.getDetail(UserContext.getUserId(), id));
    }
}
