package com.xueyifang.cloud.file.dto;

public record StoredFile(
        String relativePath,
        String accessUrl) {
}
