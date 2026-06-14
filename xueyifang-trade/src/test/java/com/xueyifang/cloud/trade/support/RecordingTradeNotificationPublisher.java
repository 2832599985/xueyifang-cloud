package com.xueyifang.cloud.trade.support;

import com.xueyifang.cloud.trade.notification.TradeNotificationPublisher;

import java.util.ArrayList;
import java.util.List;

public class RecordingTradeNotificationPublisher implements TradeNotificationPublisher {

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void publishOrderNotification(Long recipientId, String title, String content, Long orderId) {
        notifications.add(new Notification(TYPE_ORDER, recipientId, title, content, orderId));
    }

    @Override
    public void publishDisputeNotification(Long recipientId, String title, String content, Long disputeId) {
        notifications.add(new Notification(TYPE_DISPUTE, recipientId, title, content, disputeId));
    }

    public List<Notification> notifications() {
        return notifications;
    }

    public record Notification(Integer notificationType, Long recipientId, String title, String content,
                               Long relatedId) {
    }
}
