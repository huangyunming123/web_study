package com.demo.auth.controller;

import com.demo.auth.common.R;
import com.demo.auth.dto.LoginDTO;
import com.demo.auth.dto.RegisterDTO;
import com.demo.auth.service.AuthService;
import com.demo.auth.vo.LoginVO;
import com.demo.auth.vo.VerifyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口控制器
 */
@Tag(name = "鉴权服务", description = "用户注册、登录、Token 验证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<Long> register(@RequestBody RegisterDTO dto) {
        Long userId = authService.register(dto);
        return R.ok(userId);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginDTO dto) {
        LoginVO loginVO = authService.login(dto);
        return R.ok(loginVO);
    }

    @Operation(summary = "Token 校验 (供网关或服务间鉴权)")
    @GetMapping("/verify")
    public R<VerifyVO> verify(
            @Parameter(description = "形如 'Bearer <token>'", required = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        VerifyVO verifyVO = authService.verify(authHeader);
        return R.ok(verifyVO);
    }
}
