package com.xueyifang.cloud.auth.service;

import com.xueyifang.cloud.auth.dto.LoginRequest;
import com.xueyifang.cloud.auth.dto.LoginResponse;
import com.xueyifang.cloud.auth.dto.RegisterRequest;
import com.xueyifang.cloud.auth.repository.AuthSystemConfigRepository;
import com.xueyifang.cloud.auth.repository.AuthUser;
import com.xueyifang.cloud.auth.repository.AuthUserCreateCommand;
import com.xueyifang.cloud.auth.repository.AuthUserRepository;
import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.auth.JwtToken;
import com.xueyifang.cloud.common.core.auth.JwtTokenService;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int ACTIVE_STATUS = 1;

    private static final String REGISTER_ENABLED_KEY = "REGISTER_ENABLED";

    private static final String REGISTER_ENABLED_DEFAULT = "1";

    private final AuthUserRepository userRepository;

    private final AuthSystemConfigRepository systemConfigRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    public AuthService(AuthUserRepository userRepository, AuthSystemConfigRepository systemConfigRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        if (!isRegisterEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "registration is disabled");
        }

        String username = normalizeRequired(request.username(), "username");
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXIST);
        }

        String nickname = normalizeOptional(request.nickname());
        if (nickname == null) {
            nickname = username;
        }

        AuthUserCreateCommand command = new AuthUserCreateCommand(
                username,
                passwordEncoder.encode(normalizeRequired(request.password(), "password")),
                nickname,
                normalizeOptional(request.phone()),
                normalizeOptional(request.email()),
                AuthUserRole.STUDENT.databaseValue(),
                AuthUserRole.STUDENT.publishPermission(),
                ACTIVE_STATUS);

        try {
            userRepository.create(command);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXIST);
        }
    }

    public LoginResponse login(LoginRequest request) {
        String username = normalizeRequired(request.username(), "username");
        AuthUser user = userRepository.findByUsername(username)
                .orElseThrow(this::loginFailed);

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw loginFailed();
        }
        if (!Integer.valueOf(ACTIVE_STATUS).equals(user.status())) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_DISABLED);
        }

        AuthUserRole role = AuthUserRole.fromDatabaseValue(user.role());
        int publishPermission = user.publishPermission() != null
                ? user.publishPermission()
                : role.publishPermission();
        JwtToken token = jwtTokenService.createToken(user.id(), role.tokenCode(), publishPermission);
        return LoginResponse.from(token, user, role.tokenCode(), publishPermission);
    }

    private BusinessException loginFailed() {
        return new BusinessException(ErrorCode.USER_PASSWORD_ERROR, "username or password is invalid");
    }

    private boolean isRegisterEnabled() {
        String value = systemConfigRepository.findEnabledConfigValue(REGISTER_ENABLED_KEY)
                .orElse(REGISTER_ENABLED_DEFAULT);
        return "1".equals(value.trim()) || "true".equalsIgnoreCase(value.trim());
    }

    private String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, field + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
