package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.RecentOrderResponse;
import com.xueyifang.cloud.trade.dto.SalesServiceResponse;
import com.xueyifang.cloud.trade.dto.UserSalesStatisticsResponse;
import com.xueyifang.cloud.trade.repository.BestSellingService;
import com.xueyifang.cloud.trade.repository.SellerSalesSummary;
import com.xueyifang.cloud.trade.repository.TradeOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TradeStatisticsService {

    private static final int RECENT_ORDER_LIMIT = 5;

    private final TradeOrderRepository orderRepository;

    public TradeStatisticsService(TradeOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public UserSalesStatisticsResponse getCurrentUserSalesStatistics() {
        LoginUserContext user = requireCurrentUser();
        orderRepository.findUserById(user.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        SellerSalesSummary summary = orderRepository.summarizeCompletedSalesBySeller(user.userId());
        int totalSales = summary.totalSales() != null ? summary.totalSales() : 0;
        BigDecimal totalRevenue = summary.totalRevenue() != null ? summary.totalRevenue() : BigDecimal.ZERO;
        BigDecimal averagePrice = totalSales == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP);

        SalesServiceResponse bestService = orderRepository.findBestSellingServiceBySeller(user.userId())
                .map(this::toSalesServiceResponse)
                .orElse(null);

        return new UserSalesStatisticsResponse(
                totalSales,
                totalRevenue,
                averagePrice,
                bestService,
                orderRepository.findRecentCompletedSalesBySeller(user.userId(), RECENT_ORDER_LIMIT)
                        .stream()
                        .map(order -> new RecentOrderResponse(
                                order.orderId(),
                                order.orderNumber(),
                                order.totalAmount(),
                                order.createTime()))
                        .toList());
    }

    private SalesServiceResponse toSalesServiceResponse(BestSellingService service) {
        return new SalesServiceResponse(
                service.serviceId(),
                service.serviceTitle(),
                service.sales(),
                service.revenue());
    }

    private LoginUserContext requireCurrentUser() {
        return UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
    }
}
