package com.xueyifang.cloud.common.core.auth;

import java.io.Serializable;
import java.time.Instant;

public record JwtTokenClaims(
        Long userId,
        Integer role,
        Integer publishPermission,
        Instant issuedAt,
        Instant expiresAt
) implements Serializable {

    public JwtTokenClaims {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
    }
}
