package com.xueyifang.cloud.trade.dto;

import java.math.BigDecimal;
import java.util.List;

public record UserSalesStatisticsResponse(
        Integer totalSales,
        BigDecimal totalRevenue,
        BigDecimal averagePrice,
        SalesServiceResponse bestService,
        List<RecentOrderResponse> recentOrders) {
}
