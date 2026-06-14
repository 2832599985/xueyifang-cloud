package com.xueyifang.cloud.trade.notification;

public interface TradeNotificationPublisher {

    int TYPE_ORDER = 3;

    int TYPE_DISPUTE = 4;

    void publishOrderNotification(Long recipientId, String title, String content, Long orderId);

    void publishDisputeNotification(Long recipientId, String title, String content, Long disputeId);
}
