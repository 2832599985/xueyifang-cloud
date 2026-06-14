package com.xueyifang.cloud.message.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xueyifang.cloud.message.config.MessagePushProperties;
import com.xueyifang.cloud.message.dto.ChatMessageResponse;
import com.xueyifang.cloud.message.dto.UserSimpleResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisBroadcastMessagePushServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final MessageWebSocketSessionManager sessionManager = mock(MessageWebSocketSessionManager.class);

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);

    private final MessagePushProperties properties = new MessagePushProperties();

    @Test
    void pushesLocallyAndPublishesToRedis() throws Exception {
        properties.getRedis().setChannel("message-channel");
        properties.getRedis().setInstanceId("node-a");
        RedisBroadcastMessagePushService pushService = new RedisBroadcastMessagePushService(
                sessionManager,
                redisTemplate,
                objectMapper,
                properties);

        pushService.pushChatMessage(2L, new ChatMessageResponse(
                10L,
                new UserSimpleResponse(1L, "Sender", null),
                new UserSimpleResponse(2L, "Receiver", null),
                "hello",
                1,
                0,
                LocalDateTime.of(2026, 6, 14, 12, 0)));

        ArgumentCaptor<RealtimeMessage> localMessageCaptor = ArgumentCaptor.forClass(RealtimeMessage.class);
        verify(sessionManager).sendToUser(eq(2L), localMessageCaptor.capture());
        assertThat(localMessageCaptor.getValue().type()).isEqualTo("NEW_CHAT");

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(eq("message-channel"), payloadCaptor.capture());
        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertThat(payload.get("originInstanceId").asText()).isEqualTo("node-a");
        assertThat(payload.get("userId").asLong()).isEqualTo(2L);
        assertThat(payload.get("message").get("type").asText()).isEqualTo("NEW_CHAT");
    }

    @Test
    void keepsLocalPushWhenRedisPublishFails() {
        properties.getRedis().setChannel("message-channel");
        properties.getRedis().setInstanceId("node-a");
        when(redisTemplate.convertAndSend(eq("message-channel"), org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));
        RedisBroadcastMessagePushService pushService = new RedisBroadcastMessagePushService(
                sessionManager,
                redisTemplate,
                objectMapper,
                properties);

        pushService.pushChatMessage(2L, new ChatMessageResponse(
                10L,
                new UserSimpleResponse(1L, "Sender", null),
                new UserSimpleResponse(2L, "Receiver", null),
                "hello",
                1,
                0,
                LocalDateTime.of(2026, 6, 14, 12, 0)));

        verify(sessionManager).sendToUser(eq(2L), org.mockito.ArgumentMatchers.any(RealtimeMessage.class));
    }
}
