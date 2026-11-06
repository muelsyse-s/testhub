package com.testhub.system.controller;

import com.testhub.common.R;
import com.testhub.security.CurrentUser;
import com.testhub.security.LoginUser;
import com.testhub.system.dto.LoginRequest;
import com.testhub.system.dto.LoginResponse;
import com.testhub.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口。 */
@Tag(name = "01. 认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录", description = "账号密码登录，成功后返回 JWT")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "当前用户信息")
    @GetMapping("/me")
    public R<LoginUser> me() {
        return R.ok(CurrentUser.get());
    }

    @Operation(summary = "退出登录", description = "JWT 无状态，前端清除本地 token 即可")
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
