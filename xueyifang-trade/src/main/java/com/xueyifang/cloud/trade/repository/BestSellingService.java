package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;

public record BestSellingService(
        Long serviceId,
        String serviceTitle,
        Integer sales,
        BigDecimal revenue) {
}
