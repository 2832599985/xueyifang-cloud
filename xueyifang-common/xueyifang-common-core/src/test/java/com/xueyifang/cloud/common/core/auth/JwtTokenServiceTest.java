package com.xueyifang.cloud.common.core.auth;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "xueyifang-secret-key-2025-graduation-project";

    private static final Instant NOW = Instant.parse("2026-06-14T00:00:00Z");

    @Test
    void createsAndParsesTokenClaims() {
        JwtTokenService tokenService = newTokenService(Duration.ofDays(7));

        JwtToken token = tokenService.createToken(10L, 2, 1);
        JwtTokenClaims claims = tokenService.parseToken(token.token());

        assertThat(token.tokenType()).isEqualTo(AuthConstants.TOKEN_TYPE);
        assertThat(token.expiresInSeconds()).isEqualTo(Duration.ofDays(7).toSeconds());
        assertThat(claims.userId()).isEqualTo(10L);
        assertThat(claims.role()).isEqualTo(2);
        assertThat(claims.publishPermission()).isEqualTo(1);
        assertThat(claims.issuedAt()).isEqualTo(NOW);
        assertThat(claims.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
    }

    @Test
    void refreshesTokenWithSameUserClaims() {
        JwtTokenService tokenService = newTokenService(Duration.ofDays(7));
        JwtToken token = tokenService.createToken(11L, 1, 0);

        JwtToken refreshedToken = tokenService.refreshToken(token.token());
        JwtTokenClaims refreshedClaims = tokenService.parseToken(refreshedToken.token());

        assertThat(refreshedClaims.userId()).isEqualTo(11L);
        assertThat(refreshedClaims.role()).isEqualTo(1);
        assertThat(refreshedClaims.publishPermission()).isEqualTo(0);
    }

    @Test
    void rejectsBlankToken() {
        JwtTokenService tokenService = newTokenService(Duration.ofDays(7));

        assertThatThrownBy(() -> tokenService.parseToken(" "))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.TOKEN_INVALID.getCode()));
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenService issuingService = newTokenService(Duration.ofSeconds(1));
        JwtToken token = issuingService.createToken(12L, 1, 1);
        JwtTokenService parsingService = new JwtTokenService(properties(Duration.ofSeconds(1)),
                Clock.fixed(NOW.plusSeconds(2), ZoneOffset.UTC));

        assertThatThrownBy(() -> parsingService.parseToken(token.token()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED.getCode()));
    }

    private JwtTokenService newTokenService(Duration expiration) {
        return new JwtTokenService(properties(expiration), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private JwtTokenProperties properties(Duration expiration) {
        return new JwtTokenProperties(SECRET, expiration, JwtTokenProperties.DEFAULT_ISSUER);
    }
}
