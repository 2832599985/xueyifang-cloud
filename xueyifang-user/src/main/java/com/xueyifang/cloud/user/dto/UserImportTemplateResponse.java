package com.xueyifang.cloud.user.dto;

public record UserImportTemplateResponse(
        byte[] content,
        String filename,
        String contentType) {
}
