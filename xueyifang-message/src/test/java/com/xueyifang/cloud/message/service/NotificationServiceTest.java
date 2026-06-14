package com.xueyifang.cloud.message.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.message.dto.NotificationCreateRequest;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.dto.PageResponse;
import com.xueyifang.cloud.message.support.InMemoryMessageRepository;
import com.xueyifang.cloud.message.support.RecordingMessagePushService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationServiceTest {

    private final InMemoryMessageRepository repository = new InMemoryMessageRepository();

    private final RecordingMessagePushService pushService = new RecordingMessagePushService();

    private final NotificationService notificationService = new NotificationService(repository, pushService);

    @BeforeEach
    void setUp() {
        repository.clear();
        repository.putUser(1L, "First", "/avatar/1.png", 1);
        repository.putUser(2L, "Second", "/avatar/2.png", 1);
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void createsNotificationAndPushesToRecipient() {
        Long notificationId = notificationService.createNotification(new NotificationCreateRequest(
                1L,
                MessageConstants.NOTIFICATION_TYPE_ORDER,
                "Order",
                "Paid",
                100L));

        assertThat(notificationId).isPositive();
        assertThat(pushService.notificationPushes()).hasSize(1);
        assertThat(pushService.notificationPushes().getFirst().recipientId()).isEqualTo(1L);
        assertThat(pushService.notificationPushes().getFirst().notification().title()).isEqualTo("Order");
    }

    @Test
    void listsUnreadCountAndMarksNotificationsRead() {
        Long firstId = repository.putNotification(
                1L,
                MessageConstants.NOTIFICATION_TYPE_ORDER,
                "Order",
                "Paid",
                100L,
                MessageConstants.READ_STATUS_UNREAD);
        repository.putNotification(
                1L,
                MessageConstants.NOTIFICATION_TYPE_DISPUTE,
                "Dispute",
                "Pending",
                200L,
                MessageConstants.READ_STATUS_UNREAD);
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));

        PageResponse<NotificationResponse> page = notificationService.listMyNotifications(1, 10, null);
        long unreadBefore = notificationService.getUnreadCount();
        notificationService.markAsRead(firstId);
        long unreadAfterSingleRead = notificationService.getUnreadCount();
        notificationService.markAllAsRead();

        assertThat(page.total()).isEqualTo(2);
        assertThat(unreadBefore).isEqualTo(2);
        assertThat(unreadAfterSingleRead).isEqualTo(1);
        assertThat(notificationService.getUnreadCount()).isZero();
    }

    @Test
    void rejectsMarkingAnotherUsersNotification() {
        Long notificationId = repository.putNotification(
                2L,
                MessageConstants.NOTIFICATION_TYPE_ORDER,
                "Order",
                "Paid",
                100L,
                MessageConstants.READ_STATUS_UNREAD);
        UserContextHolder.set(new LoginUserContext(1L, 1, 1));

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    @Test
    void rejectsUnsupportedNotificationType() {
        assertThatThrownBy(() -> notificationService.createNotification(new NotificationCreateRequest(
                1L,
                99,
                "Bad",
                "Bad",
                null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void requiresLoginForNotificationReads() {
        assertThatThrownBy(() -> notificationService.getUnreadCount())
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode()));
    }
}
