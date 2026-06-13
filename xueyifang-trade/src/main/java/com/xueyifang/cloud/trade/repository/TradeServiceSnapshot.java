package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;

public record TradeServiceSnapshot(
        Long serviceId,
        Long publisherId,
        String title,
        String description,
        BigDecimal price,
        String coverImage,
        Integer status) {
}
