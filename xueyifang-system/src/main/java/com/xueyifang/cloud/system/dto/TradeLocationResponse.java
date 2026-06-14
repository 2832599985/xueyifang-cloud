package com.xueyifang.cloud.system.dto;

import com.xueyifang.cloud.system.repository.TradeLocationItem;

import java.time.LocalDateTime;

public record TradeLocationResponse(
        Long id,
        String locationName,
        String locationDescription,
        String locationAddress,
        Integer isAvailable,
        LocalDateTime createTime,
        LocalDateTime updateTime) {

    public static TradeLocationResponse from(TradeLocationItem item) {
        return new TradeLocationResponse(
                item.id(),
                item.locationName(),
                item.locationDescription(),
                item.locationAddress(),
                item.isAvailable(),
                item.createTime(),
                item.updateTime());
    }
}
