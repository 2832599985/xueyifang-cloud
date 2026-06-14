package com.xueyifang.cloud.user.notification;

import com.xueyifang.cloud.user.config.UserNotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpUserNotificationPublisher implements UserNotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpUserNotificationPublisher.class);

    private final RestClient restClient;

    private final UserNotificationProperties properties;

    public HttpUserNotificationPublisher(RestClient userMessageRestClient,
                                         UserNotificationProperties properties) {
        this.restClient = userMessageRestClient;
        this.properties = properties;
    }

    @Override
    public void publishPermissionReviewNotification(Long recipientId, String title, String content) {
        publish(new InternalNotificationRequest(
                recipientId,
                TYPE_PERMISSION_APPROVAL,
                title,
                content,
                null));
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
            log.warn("Failed to publish user notification to user {}", request.recipientId(), exception);
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
