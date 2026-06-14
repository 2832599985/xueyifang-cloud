package com.xueyifang.cloud.system.dto;

import com.xueyifang.cloud.system.repository.ProfessionalItem;

import java.time.LocalDateTime;

public record ProfessionalResponse(
        Long id,
        String professionalName,
        String description,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static ProfessionalResponse from(ProfessionalItem item) {
        return new ProfessionalResponse(
                item.id(),
                item.professionalName(),
                item.description(),
                item.createTime(),
                item.updateTime());
    }
}
