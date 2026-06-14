package com.xueyifang.cloud.system.repository;

import java.time.LocalDateTime;

public record ProfessionalItem(
        Long id,
        String professionalName,
        String description,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
