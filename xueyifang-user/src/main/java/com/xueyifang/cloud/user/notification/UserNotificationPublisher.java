package com.xueyifang.cloud.user.notification;

public interface UserNotificationPublisher {

    int TYPE_PERMISSION_APPROVAL = 1;

    void publishPermissionReviewNotification(Long recipientId, String title, String content);
}
