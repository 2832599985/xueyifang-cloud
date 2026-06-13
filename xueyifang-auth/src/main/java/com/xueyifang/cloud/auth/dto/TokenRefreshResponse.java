package com.xueyifang.cloud.auth.dto;

import com.xueyifang.cloud.common.core.auth.JwtToken;

import java.io.Serializable;
import java.time.Instant;

public record TokenRefreshResponse(
        String token,
        String tokenType,
        long expiresIn,
        Instant expiresAt
) implements Serializable {

    public static TokenRefreshResponse from(JwtToken token) {
        return new TokenRefreshResponse(
                token.token(),
                token.tokenType(),
                token.expiresInSeconds(),
                token.expiresAt());
    }
}
