package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.service.EncyclopediaService;
import com.kepuxingqiu.vo.EncyclopediaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "图鉴模块", description = "科普图鉴列表与详情")
@RestController
@RequestMapping("/api/encyclopedia")
public class EncyclopediaController {

    private final EncyclopediaService encyclopediaService;

    public EncyclopediaController(EncyclopediaService encyclopediaService) {
        this.encyclopediaService = encyclopediaService;
    }

    @Operation(summary = "获取图鉴列表")
    @GetMapping("/list")
    public Result<List<EncyclopediaVO>> list(
            @Parameter(description = "分类: all/astronomy/history/insect")
            @RequestParam(defaultValue = "all") String category) {
        return Result.success(encyclopediaService.list(UserContext.getUserId(), category));
    }

    @Operation(summary = "获取图鉴详情")
    @GetMapping("/detail/{id}")
    public Result<EncyclopediaVO> getDetail(@PathVariable Long id) {
        return Result.success(encyclopediaService.getDetail(UserContext.getUserId(), id));
    }
}
