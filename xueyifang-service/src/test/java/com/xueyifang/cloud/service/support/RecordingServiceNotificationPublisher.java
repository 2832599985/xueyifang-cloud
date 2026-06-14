package com.xueyifang.cloud.service.support;

import com.xueyifang.cloud.service.notification.ServiceNotificationPublisher;

import java.util.ArrayList;
import java.util.List;

public class RecordingServiceNotificationPublisher implements ServiceNotificationPublisher {

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void publishServiceReviewNotification(Long recipientId, Long serviceId, String title, String content) {
        notifications.add(new Notification(recipientId, serviceId, title, content));
    }

    public List<Notification> notifications() {
        return notifications;
    }

    public record Notification(Long recipientId, Long serviceId, String title, String content) {
    }
}
