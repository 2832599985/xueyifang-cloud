package com.xueyifang.cloud.service.repository;

public record ServiceListQuery(
        String keyword,
        Long tagId,
        Long categoryId,
        Long professionalId,
        Long publisherId,
        Integer status,
        int offset,
        int limit) {
}
