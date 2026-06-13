package com.xueyifang.cloud.auth.service;

import com.xueyifang.cloud.auth.support.InMemoryTokenBlacklistService;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.auth.JwtToken;
import com.xueyifang.cloud.common.core.auth.JwtTokenProperties;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthTokenServiceTest {

    private static final String SECRET = "xueyifang-secret-key-2025-graduation-project";

    private static final Instant NOW = Instant.parse("2026-06-14T00:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            new JwtTokenProperties(SECRET, Duration.ofDays(7), JwtTokenProperties.DEFAULT_ISSUER),
            clock);

    private final InMemoryTokenBlacklistService blacklistService = new InMemoryTokenBlacklistService(clock);

    private final AuthTokenService authTokenService = new AuthTokenService(jwtTokenService, blacklistService);

    @Test
    void logoutBlacklistsTokenUntilExpiration() {
        JwtToken token = jwtTokenService.createToken(21L, 2, 1);

        authTokenService.logout(token.token());

        assertThat(blacklistService.isBlacklisted(token.token())).isTrue();
    }

    @Test
    void rejectsBlacklistedTokenRefresh() {
        JwtToken token = jwtTokenService.createToken(21L, 2, 1);
        blacklistService.blacklist(token.token(), token.expiresAt());

        assertThatThrownBy(() -> authTokenService.refreshToken(token.token()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.TOKEN_INVALID.getCode()));
    }
}
