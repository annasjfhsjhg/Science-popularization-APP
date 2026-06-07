package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.dto.UpdateProfileDTO;
import com.kepuxingqiu.service.UserService;
import com.kepuxingqiu.vo.UserInfoVO;
import com.kepuxingqiu.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块", description = "个人信息/学习报告")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取用户信息（前端主接口）")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(userService.getUserInfo(UserContext.getUserId()));
    }

    @Operation(summary = "获取个人详细信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        return Result.success(userService.getProfile(UserContext.getUserId()));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        userService.updateProfile(UserContext.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "获取学习报告")
    @GetMapping("/report")
    public Result<UserProfileVO> getLearningReport() {
        return Result.success(userService.getLearningReport(UserContext.getUserId()));
    }
}
