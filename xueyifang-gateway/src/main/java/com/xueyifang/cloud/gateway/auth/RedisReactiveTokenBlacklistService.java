package com.xueyifang.cloud.gateway.auth;

import com.xueyifang.cloud.common.core.auth.TokenBlacklistKeys;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RedisReactiveTokenBlacklistService implements ReactiveTokenBlacklistService {

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisReactiveTokenBlacklistService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Boolean> isBlacklisted(String token) {
        return redisTemplate.hasKey(TokenBlacklistKeys.fromToken(token))
                .onErrorReturn(false)
                .defaultIfEmpty(false);
    }
}
