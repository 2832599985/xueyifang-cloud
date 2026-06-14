package com.xueyifang.cloud.trade.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.trade.dto.UserSalesStatisticsResponse;
import com.xueyifang.cloud.trade.service.TradeStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final TradeStatisticsService tradeStatisticsService;

    public StatisticsController(TradeStatisticsService tradeStatisticsService) {
        this.tradeStatisticsService = tradeStatisticsService;
    }

    @GetMapping("/sales")
    public BaseResponse<UserSalesStatisticsResponse> getSalesStatistics() {
        return ResultUtils.success(tradeStatisticsService.getCurrentUserSalesStatistics());
    }
}
