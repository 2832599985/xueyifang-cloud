package com.xueyifang.cloud.message.websocket;

public record RealtimeMessage(
        String type,
        Object data) {

    public static RealtimeMessage connected(Long userId) {
        return new RealtimeMessage("CONNECTED", userId);
    }

    public static RealtimeMessage pong() {
        return new RealtimeMessage("PONG", null);
    }

    public static RealtimeMessage newChat(Object data) {
        return new RealtimeMessage("NEW_CHAT", data);
    }

    public static RealtimeMessage newNotification(Object data) {
        return new RealtimeMessage("NEW_NOTIFICATION", data);
    }
}
