package com.xueyifang.cloud.user.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.user.dto.AdminPermissionReviewRequest;
import com.xueyifang.cloud.user.dto.PageResponse;
import com.xueyifang.cloud.user.dto.PendingPermissionUserResponse;
import com.xueyifang.cloud.user.dto.PermissionReviewResponse;
import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.support.InMemoryUserAccountRepository;
import com.xueyifang.cloud.user.support.RecordingUserNotificationPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminPermissionServiceTest {

    private final InMemoryUserAccountRepository repository = new InMemoryUserAccountRepository();

    private final RecordingUserNotificationPublisher notificationPublisher =
            new RecordingUserNotificationPublisher();

    private final AdminPermissionService service =
            new AdminPermissionService(repository, notificationPublisher);

    @BeforeEach
    void setUp() {
        repository.put(user(1L, 0, 0, "need to publish"));
        repository.put(user(2L, 1, 1, null));
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void listsPendingPermissionUsersForAdmin() {
        PageResponse<PendingPermissionUserResponse> page = service.listPendingUsers(1, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records().getFirst().userId()).isEqualTo(1L);
        assertThat(page.records().getFirst().permissionApplyReason()).isEqualTo("need to publish");
    }

    @Test
    void approvesPermissionAndPublishesNotification() {
        PermissionReviewResponse response = service.reviewPermission(
                new AdminPermissionReviewRequest(1L, true, null));

        assertThat(response.status()).isEqualTo("approved");
        assertThat(repository.findById(1L).orElseThrow().publishPermission()).isEqualTo(1);
        assertThat(notificationPublisher.notifications()).hasSize(1);
        assertThat(notificationPublisher.notifications().getFirst().recipientId()).isEqualTo(1L);
        assertThat(notificationPublisher.notifications().getFirst().title()).contains("通过");
    }

    @Test
    void rejectsPermissionWithReason() {
        PermissionReviewResponse response = service.reviewPermission(
                new AdminPermissionReviewRequest(1L, false, "资料不完整"));

        assertThat(response.status()).isEqualTo("rejected");
        assertThat(repository.findById(1L).orElseThrow().permissionReviewReason()).isEqualTo("资料不完整");
        assertThat(notificationPublisher.notifications().getFirst().content()).contains("资料不完整");
    }

    @Test
    void rejectsReviewByNormalUser() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        assertThatThrownBy(() -> service.listPendingUsers(1, 10))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    @Test
    void rejectsRepeatedReview() {
        assertThatThrownBy(() -> service.reviewPermission(
                new AdminPermissionReviewRequest(2L, true, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.OPERATION_ERROR.getCode()));
    }

    private UserAccount user(Long id, Integer publishPermission, Integer permissionReviewStatus,
                             String applyReason) {
        return new UserAccount(
                id,
                "student_" + id,
                "encoded",
                "student_" + id,
                "Student " + id,
                "Student " + id,
                "1380013800" + id,
                "student" + id + "@example.com",
                null,
                "junior",
                1L,
                null,
                null,
                "STUDENT",
                publishPermission,
                permissionReviewStatus,
                applyReason,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1,
                1,
                LocalDateTime.parse("2026-06-14T00:00:00").plusMinutes(id),
                LocalDateTime.parse("2026-06-14T00:00:00").plusMinutes(id));
    }
}
