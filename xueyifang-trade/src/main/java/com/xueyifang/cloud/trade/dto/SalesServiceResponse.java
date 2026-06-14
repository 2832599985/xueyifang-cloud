package com.xueyifang.cloud.trade.dto;

import java.math.BigDecimal;

public record SalesServiceResponse(
        Long serviceId,
        String serviceTitle,
        Integer sales,
        BigDecimal revenue) {
}
