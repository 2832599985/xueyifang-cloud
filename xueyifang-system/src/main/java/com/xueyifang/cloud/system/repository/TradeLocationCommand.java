package com.xueyifang.cloud.system.repository;

public record TradeLocationCommand(
        String locationName,
        String locationDescription,
        String locationAddress,
        Integer isAvailable) {
}
