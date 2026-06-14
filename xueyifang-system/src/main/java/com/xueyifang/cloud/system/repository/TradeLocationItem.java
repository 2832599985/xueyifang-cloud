package com.xueyifang.cloud.system.repository;

import java.time.LocalDateTime;

public record TradeLocationItem(
        Long id,
        String locationName,
        String locationDescription,
        String locationAddress,
        Integer isAvailable,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
