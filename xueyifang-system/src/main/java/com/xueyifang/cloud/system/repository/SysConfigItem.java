package com.xueyifang.cloud.system.repository;

import java.time.LocalDateTime;

public record SysConfigItem(
        Long id,
        String configKey,
        String configValue,
        String description,
        Integer isEnabled,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
