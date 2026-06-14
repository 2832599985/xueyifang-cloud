package com.xueyifang.cloud.system.dto;

import com.xueyifang.cloud.system.repository.SysConfigItem;

import java.time.LocalDateTime;

public record SysConfigResponse(
        Long id,
        String configKey,
        String configValue,
        String description,
        Integer isEnabled,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static SysConfigResponse from(SysConfigItem item) {
        return new SysConfigResponse(
                item.id(),
                item.configKey(),
                item.configValue(),
                item.description(),
                item.isEnabled(),
                item.createTime(),
                item.updateTime());
    }
}
