package com.xueyifang.cloud.trade.notification;

import com.xueyifang.cloud.trade.config.TradeNotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpTradeNotificationPublisher implements TradeNotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpTradeNotificationPublisher.class);

    private final RestClient restClient;

    private final TradeNotificationProperties properties;

    public HttpTradeNotificationPublisher(RestClient tradeMessageRestClient,
                                          TradeNotificationProperties properties) {
        this.restClient = tradeMessageRestClient;
        this.properties = properties;
    }

    @Override
    public void publishOrderNotification(Long recipientId, String title, String content, Long orderId) {
        publish(new InternalNotificationRequest(recipientId, TYPE_ORDER, title, content, orderId));
    }

    @Override
    public void publishDisputeNotification(Long recipientId, String title, String content, Long disputeId) {
        publish(new InternalNotificationRequest(recipientId, TYPE_DISPUTE, title, content, disputeId));
    }

    private void publish(InternalNotificationRequest request) {
        if (!properties.isEnabled() || request.recipientId() == null) {
            return;
        }
        Runnable task = () -> send(request);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }

    private void send(InternalNotificationRequest request) {
        try {
            restClient.post()
                    .uri(properties.normalizedCreatePath())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.warn("Failed to publish trade notification to user {}", request.recipientId(), exception);
        }
    }

    private record InternalNotificationRequest(
            Long recipientId,
            Integer notificationType,
            String title,
            String content,
            Long relatedId) {
    }
}
