package com.xueyifang.cloud.service.notification;

public interface ServiceNotificationPublisher {

    int TYPE_SERVICE_REVIEW = 5;

    void publishServiceReviewNotification(Long recipientId, Long serviceId, String title, String content);
}
