package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.service.GameService;
import com.kepuxingqiu.vo.HomeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "首页模块", description = "首页数据聚合")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final GameService gameService;

    public HomeController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "获取首页数据（用户等级卡/游戏分类/每日推荐）")
    @GetMapping("/dashboard")
    public Result<HomeVO> getDashboard() {
        return Result.success(gameService.getHomeDashboard(UserContext.getUserId()));
    }
}
