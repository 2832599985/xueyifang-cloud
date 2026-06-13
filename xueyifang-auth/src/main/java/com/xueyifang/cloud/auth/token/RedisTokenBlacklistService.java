package com.xueyifang.cloud.auth.token;

import com.xueyifang.cloud.common.core.auth.TokenBlacklistKeys;
import com.xueyifang.cloud.common.core.auth.TokenBlacklistService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private final Clock clock;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this(redisTemplate, Clock.systemUTC());
    }

    RedisTokenBlacklistService(StringRedisTemplate redisTemplate, Clock clock) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }

    @Override
    public boolean isBlacklisted(String token) {
        Boolean exists = redisTemplate.hasKey(TokenBlacklistKeys.fromToken(token));
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void blacklist(String token, Instant expiresAt) {
        if (expiresAt == null) {
            return;
        }
        Duration ttl = Duration.between(clock.instant(), expiresAt);
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(TokenBlacklistKeys.fromToken(token), "1", ttl);
    }
}
