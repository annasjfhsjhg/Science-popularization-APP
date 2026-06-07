package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.dto.GameResultDTO;
import com.kepuxingqiu.dto.GameSubmitDTO;
import com.kepuxingqiu.service.GameService;
import com.kepuxingqiu.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "游戏模块", description = "关卡列表/详情/提交结果")
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "获取分类下的关卡列表")
    @GetMapping("/content/{categoryId}")
    public Result<List<GameContentVO>> listByCategory(
            @Parameter(description = "分类ID") @PathVariable Integer categoryId) {
        return Result.success(gameService.listByCategory(UserContext.getUserId(), categoryId));
    }

    @Operation(summary = "获取关卡详情（含游戏配置数据）")
    @GetMapping("/content/detail/{contentId}")
    public Result<GameContentVO> getDetail(
            @Parameter(description = "关卡ID") @PathVariable Long contentId) {
        return Result.success(gameService.getDetail(UserContext.getUserId(), contentId));
    }

    @Operation(summary = "提交游戏结果（旧版，保留兼容）")
    @PostMapping("/submit")
    public Result<GameSubmitResultVO> submitGame(@Valid @RequestBody GameSubmitDTO dto) {
        return Result.success(gameService.submitGame(UserContext.getUserId(), dto));
    }

    @Operation(summary = "获取游戏关卡数据")
    @GetMapping("/level")
    public Result<GameLevelVO> getLevel(
            @Parameter(description = "游戏类型: astronomy/history/insect")
            @RequestParam String gameType) {
        return Result.success(gameService.getLevel(gameType, UserContext.getUserId()));
    }

    @Operation(summary = "提交游戏结果（前端主接口）")
    @PostMapping("/result")
    public Result<GameResultVO> submitGameResult(@Valid @RequestBody GameResultDTO dto) {
        return Result.success(gameService.submitGameResult(UserContext.getUserId(), dto));
    }
}
