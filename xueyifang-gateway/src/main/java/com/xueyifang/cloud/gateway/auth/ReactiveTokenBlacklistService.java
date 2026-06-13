package com.xueyifang.cloud.gateway.auth;

import reactor.core.publisher.Mono;

public interface ReactiveTokenBlacklistService {

    Mono<Boolean> isBlacklisted(String token);
}
