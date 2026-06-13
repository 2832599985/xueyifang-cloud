package com.xueyifang.cloud.auth.controller;

import com.xueyifang.cloud.auth.dto.TokenRefreshResponse;
import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.AuthTokenUtils;
import com.xueyifang.cloud.common.core.auth.JwtToken;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/token")
public class AuthTokenController {

    private final JwtTokenService jwtTokenService;

    public AuthTokenController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/refresh")
    public BaseResponse<TokenRefreshResponse> refresh(
            @RequestHeader(value = AuthConstants.AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestHeader(value = AuthConstants.LEGACY_TOKEN_HEADER, required = false) String legacyToken) {
        String token = AuthTokenUtils.resolveToken(authorization, legacyToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "请先登录"));

        JwtToken refreshedToken = jwtTokenService.refreshToken(token);
        return ResultUtils.success(TokenRefreshResponse.from(refreshedToken));
    }
}
