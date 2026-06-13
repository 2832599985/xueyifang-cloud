package com.xueyifang.cloud.auth.support;

import com.xueyifang.cloud.common.core.auth.TokenBlacklistKeys;
import com.xueyifang.cloud.common.core.auth.TokenBlacklistService;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Clock clock;

    private final Map<String, Instant> expiresAtByKey = new HashMap<>();

    public InMemoryTokenBlacklistService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isBlacklisted(String token) {
        Instant expiresAt = expiresAtByKey.get(TokenBlacklistKeys.fromToken(token));
        return expiresAt != null && expiresAt.isAfter(clock.instant());
    }

    @Override
    public void blacklist(String token, Instant expiresAt) {
        expiresAtByKey.put(TokenBlacklistKeys.fromToken(token), expiresAt);
    }
}
