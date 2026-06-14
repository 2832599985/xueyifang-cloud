package com.xueyifang.cloud.user.support;

import com.xueyifang.cloud.user.notification.UserNotificationPublisher;

import java.util.ArrayList;
import java.util.List;

public class RecordingUserNotificationPublisher implements UserNotificationPublisher {

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void publishPermissionReviewNotification(Long recipientId, String title, String content) {
        notifications.add(new Notification(recipientId, title, content));
    }

    public List<Notification> notifications() {
        return notifications;
    }

    public record Notification(Long recipientId, String title, String content) {
    }
}
