package com.xueyifang.cloud.file.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record FileView(
        Resource resource,
        String filename,
        MediaType contentType,
        long contentLength) {
}
