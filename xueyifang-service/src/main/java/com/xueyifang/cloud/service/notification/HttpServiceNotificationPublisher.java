package com.xueyifang.cloud.service.notification;

import com.xueyifang.cloud.service.config.ServiceNotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpServiceNotificationPublisher implements ServiceNotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpServiceNotificationPublisher.class);

    private final RestClient restClient;

    private final ServiceNotificationProperties properties;

    public HttpServiceNotificationPublisher(RestClient serviceMessageRestClient,
                                            ServiceNotificationProperties properties) {
        this.restClient = serviceMessageRestClient;
        this.properties = properties;
    }

    @Override
    public void publishServiceReviewNotification(Long recipientId, Long serviceId, String title, String content) {
        publish(new InternalNotificationRequest(
                recipientId,
                TYPE_SERVICE_REVIEW,
                title,
                content,
                serviceId));
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
            log.warn("Failed to publish service notification to user {}", request.recipientId(), exception);
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
