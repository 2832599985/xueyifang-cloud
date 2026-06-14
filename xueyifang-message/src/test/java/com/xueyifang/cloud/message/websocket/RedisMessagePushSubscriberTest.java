package com.xueyifang.cloud.message.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xueyifang.cloud.message.config.MessagePushProperties;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.DefaultMessage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RedisMessagePushSubscriberTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final MessageWebSocketSessionManager sessionManager = mock(MessageWebSocketSessionManager.class);

    private final MessagePushProperties properties = new MessagePushProperties();

    @Test
    void deliversMessageFromAnotherInstance() throws Exception {
        properties.getRedis().setInstanceId("node-b");
        RedisMessagePushSubscriber subscriber = new RedisMessagePushSubscriber(sessionManager, objectMapper, properties);
        RedisRealtimePushMessage redisMessage = new RedisRealtimePushMessage(
                "node-a",
                2L,
                RealtimeMessage.newNotification(new NotificationResponse(
                        10L,
                        3,
                        "Order",
                        "Paid",
                        100L,
                        0,
                        LocalDateTime.of(2026, 6, 14, 12, 0))));

        subscriber.onMessage(toRedisMessage(redisMessage), null);

        ArgumentCaptor<RealtimeMessage> messageCaptor = ArgumentCaptor.forClass(RealtimeMessage.class);
        verify(sessionManager).sendToUser(eq(2L), messageCaptor.capture());
        assertThat(messageCaptor.getValue().type()).isEqualTo("NEW_NOTIFICATION");
    }

    @Test
    void ignoresMessageFromCurrentInstance() throws Exception {
        properties.getRedis().setInstanceId("node-a");
        RedisMessagePushSubscriber subscriber = new RedisMessagePushSubscriber(sessionManager, objectMapper, properties);
        RedisRealtimePushMessage redisMessage = new RedisRealtimePushMessage(
                "node-a",
                2L,
                RealtimeMessage.newNotification(new NotificationResponse(
                        10L,
                        3,
                        "Order",
                        "Paid",
                        100L,
                        0,
                        LocalDateTime.of(2026, 6, 14, 12, 0))));

        subscriber.onMessage(toRedisMessage(redisMessage), null);

        verify(sessionManager, never()).sendToUser(eq(2L), org.mockito.ArgumentMatchers.any(RealtimeMessage.class));
    }

    private DefaultMessage toRedisMessage(RedisRealtimePushMessage redisMessage) throws Exception {
        byte[] channel = "message-channel".getBytes(StandardCharsets.UTF_8);
        byte[] body = objectMapper.writeValueAsBytes(redisMessage);
        return new DefaultMessage(channel, body);
    }
}
