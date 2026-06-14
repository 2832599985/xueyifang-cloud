package com.xueyifang.cloud.message.websocket;

public record RedisRealtimePushMessage(
        String originInstanceId,
        Long userId,
        RealtimeMessage message) {
}
