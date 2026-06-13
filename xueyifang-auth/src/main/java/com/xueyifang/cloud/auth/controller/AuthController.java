package com.xueyifang.cloud.auth.controller;

import com.xueyifang.cloud.auth.dto.LoginRequest;
import com.xueyifang.cloud.auth.dto.LoginResponse;
import com.xueyifang.cloud.auth.dto.RegisterRequest;
import com.xueyifang.cloud.auth.service.AuthService;
import com.xueyifang.cloud.auth.service.AuthTokenService;
import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.AuthTokenUtils;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final AuthTokenService authTokenService;

    public AuthController(AuthService authService, AuthTokenService authTokenService) {
        this.authService = authService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/register")
    public BaseResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResultUtils.success();
    }

    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResultUtils.success(authService.login(request));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(
            @RequestHeader(value = AuthConstants.AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestHeader(value = AuthConstants.LEGACY_TOKEN_HEADER, required = false) String legacyToken) {
        String token = AuthTokenUtils.resolveToken(authorization, legacyToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
        authTokenService.logout(token);
        return ResultUtils.success();
    }
}
