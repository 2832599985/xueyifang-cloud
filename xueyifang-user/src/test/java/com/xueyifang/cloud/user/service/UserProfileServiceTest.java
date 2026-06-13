package com.xueyifang.cloud.user.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.user.dto.ChangePasswordRequest;
import com.xueyifang.cloud.user.dto.PermissionApplyRequest;
import com.xueyifang.cloud.user.dto.PermissionStatusResponse;
import com.xueyifang.cloud.user.dto.UpdateProfileRequest;
import com.xueyifang.cloud.user.dto.UserProfileResponse;
import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.support.InMemoryUserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProfileServiceTest {

    private final InMemoryUserAccountRepository userRepository = new InMemoryUserAccountRepository();

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserProfileService userProfileService = new UserProfileService(userRepository, passwordEncoder);

    @BeforeEach
    void setUp() {
        userRepository.put(user(
                1L,
                "student_001",
                "student@example.com",
                "13800138000",
                1,
                1));
        userRepository.put(user(
                2L,
                "student_002",
                "used@example.com",
                "13800138001",
                0,
                2));
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getsCurrentUserFromTrustedContext() {
        UserProfileResponse response = userProfileService.getCurrentUser();

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("student_001");
        assertThat(response.studentId()).isEqualTo("student_001");
        assertThat(response.roleCode()).isEqualTo(1);
        assertThat(response.publishPermission()).isEqualTo(1);
        assertThat(response.accountStatus()).isEqualTo(1);
    }

    @Test
    void updatesCurrentUserProfile() {
        userProfileService.updateCurrentUserProfile(new UpdateProfileRequest(
                "Tester",
                "New Nick",
                "13800138002",
                "new@example.com",
                "Dorm 2",
                "junior",
                10L,
                "avatar.png",
                "bio"));

        UserProfileResponse response = userProfileService.getCurrentUser();

        assertThat(response.realName()).isEqualTo("Tester");
        assertThat(response.nickname()).isEqualTo("New Nick");
        assertThat(response.phone()).isEqualTo("13800138002");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.professionalId()).isEqualTo(10L);
    }

    @Test
    void rejectsDuplicateEmailDuringProfileUpdate() {
        assertThatThrownBy(() -> userProfileService.updateCurrentUserProfile(new UpdateProfileRequest(
                null,
                null,
                null,
                "used@example.com",
                null,
                null,
                null,
                null,
                null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void changesPasswordWhenOldPasswordMatches() {
        userProfileService.changeCurrentUserPassword(
                new ChangePasswordRequest("secret123", "newSecret123", "newSecret123"));

        UserAccount updated = userRepository.findById(1L).orElseThrow();

        assertThat(passwordEncoder.matches("newSecret123", updated.password())).isTrue();
    }

    @Test
    void rejectsIncorrectOldPassword() {
        assertThatThrownBy(() -> userProfileService.changeCurrentUserPassword(
                new ChangePasswordRequest("badSecret", "newSecret123", "newSecret123")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void returnsPermissionStatus() {
        PermissionStatusResponse response = userProfileService.getPermissionStatus();

        assertThat(response.status()).isEqualTo("approved");
        assertThat(response.hasPermission()).isTrue();
    }

    @Test
    void appliesPermissionWhenPreviouslyRejected() {
        UserContextHolder.set(new LoginUserContext(2L, 1, 0));

        assertThat(userProfileService.applyPermission(new PermissionApplyRequest("need to publish")).status())
                .isEqualTo("pending");
        assertThat(userProfileService.getPermissionStatus().status()).isEqualTo("pending");
    }

    @Test
    void rejectsMissingLoginContext() {
        UserContextHolder.clear();

        assertThatThrownBy(userProfileService::getCurrentUser)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode()));
    }

    private UserAccount user(Long id, String username, String email, String phone,
                             Integer publishPermission, Integer permissionReviewStatus) {
        return new UserAccount(
                id,
                username,
                passwordEncoder.encode("secret123"),
                null,
                null,
                username,
                phone,
                email,
                null,
                null,
                null,
                null,
                null,
                "STUDENT",
                publishPermission,
                permissionReviewStatus,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1,
                1,
                LocalDateTime.parse("2026-06-14T00:00:00"),
                LocalDateTime.parse("2026-06-14T00:00:00"));
    }
}
