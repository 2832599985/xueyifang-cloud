package com.xueyifang.cloud.message.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xueyifang.cloud.message.config.MessagePushProperties;
import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.NotificationResponse;
import com.xueyifang.cloud.message.service.MessagePushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "xueyifang.message.push.redis", name = "enabled", havingValue = "true")
public class RedisBroadcastMessagePushService implements MessagePushService {

    private static final Logger log = LoggerFactory.getLogger(RedisBroadcastMessagePushService.class);

    private final MessageWebSocketSessionManager sessionManager;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final MessagePushProperties properties;

    public RedisBroadcastMessagePushService(MessageWebSocketSessionManager sessionManager,
                                            StringRedisTemplate redisTemplate,
                                            ObjectMapper objectMapper,
                                            MessagePushProperties properties) {
        this.sessionManager = sessionManager;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void pushChatMessage(Long receiverId, ChatMessageResponse message) {
        push(receiverId, RealtimeMessage.newChat(message));
    }

    @Override
    public void pushNotification(Long recipientId, NotificationResponse notification) {
        push(recipientId, RealtimeMessage.newNotification(notification));
    }

    private void push(Long userId, RealtimeMessage message) {
        sessionManager.sendToUser(userId, message);
        RedisRealtimePushMessage redisMessage = new RedisRealtimePushMessage(
                properties.getRedis().getInstanceId(),
                userId,
                message);
        try {
            String payload = objectMapper.writeValueAsString(redisMessage);
            redisTemplate.convertAndSend(properties.getRedis().getChannel(), payload);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize redis websocket message for userId={}", userId, exception);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish redis websocket message for userId={}, reason={}",
                    userId,
                    exception.getMessage());
        }
    }
}
