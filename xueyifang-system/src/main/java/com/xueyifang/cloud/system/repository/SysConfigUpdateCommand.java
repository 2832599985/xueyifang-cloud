package com.xueyifang.cloud.system.repository;

public record SysConfigUpdateCommand(
        String configValue,
        String description,
        Integer isEnabled) {

    public boolean hasChanges() {
        return configValue != null || description != null || isEnabled != null;
    }
}
