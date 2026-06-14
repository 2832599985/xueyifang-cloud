package com.xueyifang.cloud.system.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminTrendResponse(
        List<String> dates,
        List<Integer> orderCounts,
        List<Integer> serviceCounts,
        List<BigDecimal> transactionAmounts) {
}
