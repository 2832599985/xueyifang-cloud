package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.ServiceTag;

public record ServiceTagResponse(
        Long id,
        Long tagId,
        String name,
        Integer sortOrder) {

    public static ServiceTagResponse from(ServiceTag tag) {
        return new ServiceTagResponse(
                tag.id(),
                tag.id(),
                tag.name(),
                tag.sortOrder());
    }
}
