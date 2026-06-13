package com.xueyifang.cloud.common.core.auth;

import java.time.Instant;

public interface TokenBlacklistService {

    boolean isBlacklisted(String token);

    void blacklist(String token, Instant expiresAt);
}
