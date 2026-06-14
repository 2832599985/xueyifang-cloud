package com.xueyifang.cloud.file.service;

import java.util.Arrays;
import java.util.Optional;

public enum FileUploadBizType {

    USER_AVATAR("user_avatar", 1024L * 1024),
    SERVICE_IMAGE("service_image", 5L * 1024 * 1024);

    private final String value;

    private final long defaultMaxSize;

    FileUploadBizType(String value, long defaultMaxSize) {
        this.value = value;
        this.defaultMaxSize = defaultMaxSize;
    }

    public String value() {
        return value;
    }

    public long defaultMaxSize() {
        return defaultMaxSize;
    }

    public static Optional<FileUploadBizType> from(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalizedValue = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.value.equals(normalizedValue))
                .findFirst();
    }
}
