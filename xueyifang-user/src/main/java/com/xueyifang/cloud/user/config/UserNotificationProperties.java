package com.xueyifang.cloud.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xueyifang.user.notification")
public class UserNotificationProperties {

    private boolean enabled = true;

    private String messageBaseUrl = "http://xueyifang-message";

    private String createPath = "/internal/notifications";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMessageBaseUrl() {
        return messageBaseUrl;
    }

    public void setMessageBaseUrl(String messageBaseUrl) {
        this.messageBaseUrl = messageBaseUrl;
    }

    public String getCreatePath() {
        return createPath;
    }

    public void setCreatePath(String createPath) {
        this.createPath = createPath;
    }

    public String normalizedMessageBaseUrl() {
        return normalize(messageBaseUrl, "http://xueyifang-message");
    }

    public String normalizedCreatePath() {
        String normalized = normalize(createPath, "/internal/notifications");
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
