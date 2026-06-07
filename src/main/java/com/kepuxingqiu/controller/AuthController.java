package com.kepuxingqiu.controller;

import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.dto.LoginDTO;
import com.kepuxingqiu.dto.RegisterDTO;
import com.kepuxingqiu.service.UserService;
import com.kepuxingqiu.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证模块", description = "注册/登录/登出")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout(UserContext.getUserId());
        return Result.success();
    }
}
