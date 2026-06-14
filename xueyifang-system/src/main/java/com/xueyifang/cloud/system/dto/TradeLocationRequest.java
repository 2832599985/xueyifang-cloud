package com.xueyifang.cloud.system.dto;

public record TradeLocationRequest(
        Long id,
        String locationName,
        String locationDescription,
        String locationAddress,
        Integer isAvailable) {
}
