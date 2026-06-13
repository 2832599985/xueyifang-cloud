package com.xueyifang.cloud.auth.controller;

import com.xueyifang.cloud.auth.dto.TokenRefreshResponse;
import com.xueyifang.cloud.auth.service.AuthTokenService;
import com.xueyifang.cloud.auth.support.InMemoryTokenBlacklistService;
import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.JwtToken;
import com.xueyifang.cloud.common.core.auth.JwtTokenClaims;
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

class AuthTokenControllerTest {

    private static final String SECRET = "xueyifang-secret-key-2025-graduation-project";

    private static final Instant NOW = Instant.parse("2026-06-14T00:00:00Z");

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            new JwtTokenProperties(SECRET, Duration.ofDays(7), JwtTokenProperties.DEFAULT_ISSUER),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final InMemoryTokenBlacklistService tokenBlacklistService = new InMemoryTokenBlacklistService(
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final AuthTokenService authTokenService = new AuthTokenService(jwtTokenService, tokenBlacklistService);

    private final AuthTokenController controller = new AuthTokenController(authTokenService);

    @Test
    void refreshesBearerToken() {
        JwtToken token = jwtTokenService.createToken(21L, 2, 1);

        BaseResponse<TokenRefreshResponse> response = controller.refresh(
                AuthConstants.BEARER_PREFIX + token.token(),
                null);

        assertThat(response.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
        TokenRefreshResponse data = response.getData();
        assertThat(data.tokenType()).isEqualTo(AuthConstants.TOKEN_TYPE);
        assertThat(data.expiresIn()).isEqualTo(Duration.ofDays(7).toSeconds());

        JwtTokenClaims claims = jwtTokenService.parseToken(data.token());
        assertThat(claims.userId()).isEqualTo(21L);
        assertThat(claims.role()).isEqualTo(2);
        assertThat(claims.publishPermission()).isEqualTo(1);
    }

    @Test
    void rejectsMissingToken() {
        assertThatThrownBy(() -> controller.refresh(null, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode()));
    }

    @Test
    void rejectsBlacklistedToken() {
        JwtToken token = jwtTokenService.createToken(21L, 2, 1);
        tokenBlacklistService.blacklist(token.token(), token.expiresAt());

        assertThatThrownBy(() -> controller.refresh(AuthConstants.BEARER_PREFIX + token.token(), null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.TOKEN_INVALID.getCode()));
    }
}
