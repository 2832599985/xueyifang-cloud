package com.xueyifang.cloud.message.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xueyifang.cloud.message.config.MessagePushProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "xueyifang.message.push.redis", name = "enabled", havingValue = "true")
public class RedisMessagePushSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisMessagePushSubscriber.class);

    private final MessageWebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper;

    private final MessagePushProperties properties;

    public RedisMessagePushSubscriber(MessageWebSocketSessionManager sessionManager,
                                      ObjectMapper objectMapper,
                                      MessagePushProperties properties) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            RedisRealtimePushMessage pushMessage = objectMapper.readValue(payload, RedisRealtimePushMessage.class);
            if (pushMessage.userId() == null || pushMessage.message() == null) {
                return;
            }
            if (Objects.equals(properties.getRedis().getInstanceId(), pushMessage.originInstanceId())) {
                return;
            }
            sessionManager.sendToUser(pushMessage.userId(), pushMessage.message());
        } catch (IOException exception) {
            log.warn("Failed to deserialize redis websocket message", exception);
        } catch (RuntimeException exception) {
            log.warn("Failed to handle redis websocket message", exception);
        }
    }
}
