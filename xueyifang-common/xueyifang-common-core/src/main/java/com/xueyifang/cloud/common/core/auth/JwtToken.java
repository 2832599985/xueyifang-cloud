package com.xueyifang.cloud.common.core.auth;

import java.io.Serializable;
import java.time.Instant;

public record JwtToken(
        String token,
        String tokenType,
        Instant expiresAt,
        long expiresInSeconds
) implements Serializable {
}
