package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;

public record SellerSalesSummary(
        Integer totalSales,
        BigDecimal totalRevenue) {
}
