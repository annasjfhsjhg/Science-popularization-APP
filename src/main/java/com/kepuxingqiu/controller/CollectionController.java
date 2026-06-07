package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.service.EncyclopediaService;
import com.kepuxingqiu.vo.CollectionItemVO;
import com.kepuxingqiu.vo.EncyclopediaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "图鉴收集模块", description = "对应前端 /api/collection/list")
@RestController
@RequestMapping("/api/collection")
public class CollectionController {

    private final EncyclopediaService encyclopediaService;

    public CollectionController(EncyclopediaService encyclopediaService) {
        this.encyclopediaService = encyclopediaService;
    }

    @Operation(summary = "获取图鉴收集列表")
    @GetMapping("/list")
    public Result<List<CollectionItemVO>> list() {
        List<EncyclopediaVO> encList = encyclopediaService.list(UserContext.getUserId(), "all");
        List<CollectionItemVO> result = encList.stream().map(enc -> {
            CollectionItemVO item = new CollectionItemVO();
            item.setId(enc.getId());
            item.setName(enc.getName());
            item.setCategory(enc.getCategory());
            item.setUnlocked(enc.getUnlocked());
            return item;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
}
