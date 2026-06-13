package com.xueyifang.cloud.auth.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.auth.JwtToken;
import com.xueyifang.cloud.common.core.auth.JwtTokenClaims;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.auth.TokenBlacklistService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private final JwtTokenService jwtTokenService;

    private final TokenBlacklistService tokenBlacklistService;

    public AuthTokenService(JwtTokenService jwtTokenService, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public JwtToken refreshToken(String token) {
        ensureTokenIsActive(token);
        return jwtTokenService.refreshToken(token);
    }

    public void logout(String token) {
        ensureTokenIsActive(token);
        JwtTokenClaims claims = jwtTokenService.parseToken(token);
        tokenBlacklistService.blacklist(token, claims.expiresAt());
    }

    private void ensureTokenIsActive(String token) {
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token has been logged out");
        }
    }
}
