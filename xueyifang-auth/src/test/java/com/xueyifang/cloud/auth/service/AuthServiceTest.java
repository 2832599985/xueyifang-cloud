package com.xueyifang.cloud.auth.service;

import com.xueyifang.cloud.auth.dto.LoginRequest;
import com.xueyifang.cloud.auth.dto.LoginResponse;
import com.xueyifang.cloud.auth.dto.RegisterRequest;
import com.xueyifang.cloud.auth.repository.AuthUser;
import com.xueyifang.cloud.auth.support.InMemoryAuthUserRepository;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.auth.JwtTokenClaims;
import com.xueyifang.cloud.common.core.auth.JwtTokenProperties;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private static final String SECRET = "xueyifang-secret-key-2025-graduation-project";

    private static final Instant NOW = Instant.parse("2026-06-14T00:00:00Z");

    private final InMemoryAuthUserRepository userRepository = new InMemoryAuthUserRepository();

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            new JwtTokenProperties(SECRET, Duration.ofDays(7), JwtTokenProperties.DEFAULT_ISSUER),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final AuthService authService = new AuthService(userRepository, passwordEncoder, jwtTokenService);

    @Test
    void registersAndLogsInStudent() {
        authService.register(new RegisterRequest("test_user", "secret123", "Tester", "", ""));

        LoginResponse response = authService.login(new LoginRequest("test_user", "secret123"));

        assertThat(response.tokenType()).isEqualTo(AuthConstants.TOKEN_TYPE);
        assertThat(response.expiresIn()).isEqualTo(Duration.ofDays(7).toSeconds());
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("test_user");
        assertThat(response.nickname()).isEqualTo("Tester");
        assertThat(response.role()).isEqualTo("STUDENT");
        assertThat(response.roleCode()).isEqualTo(1);
        assertThat(response.publishPermission()).isEqualTo(1);

        JwtTokenClaims claims = jwtTokenService.parseToken(response.token());
        assertThat(claims.userId()).isEqualTo(1L);
        assertThat(claims.role()).isEqualTo(1);
        assertThat(claims.publishPermission()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("duplicate", "secret123", null, null, null);
        authService.register(request);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_USERNAME_EXIST.getCode()));
    }

    @Test
    void rejectsInvalidPasswordWithGenericLoginFailure() {
        authService.register(new RegisterRequest("wrong_pw", "correct123", null, null, null));

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrong_pw", "wrong123")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_PASSWORD_ERROR.getCode()));
    }

    @Test
    void rejectsDisabledUser() {
        userRepository.put(new AuthUser(
                99L,
                "disabled",
                passwordEncoder.encode("secret123"),
                "Disabled",
                "STUDENT",
                0));

        assertThatThrownBy(() -> authService.login(new LoginRequest("disabled", "secret123")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_ACCOUNT_DISABLED.getCode()));
    }
}
