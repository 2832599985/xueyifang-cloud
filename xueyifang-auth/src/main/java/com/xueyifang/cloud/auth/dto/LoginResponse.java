package com.xueyifang.cloud.auth.dto;

import com.xueyifang.cloud.auth.repository.AuthUser;
import com.xueyifang.cloud.common.core.auth.JwtToken;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        Instant expiresAt,
        Long userId,
        String username,
        String nickname,
        String role,
        Integer roleCode,
        Integer publishPermission) {

    public static LoginResponse from(JwtToken token, AuthUser user, Integer roleCode,
                                     Integer publishPermission) {
        return new LoginResponse(
                token.token(),
                token.tokenType(),
                token.expiresInSeconds(),
                token.expiresAt(),
                user.id(),
                user.username(),
                user.nickname(),
                user.role(),
                roleCode,
                publishPermission);
    }
}
