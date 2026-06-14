package com.xueyifang.cloud.system.controller;

import com.xueyifang.cloud.common.core.api.BaseResponse;
import com.xueyifang.cloud.common.core.api.ResultUtils;
import com.xueyifang.cloud.system.dto.AdminStatisticsResponse;
import com.xueyifang.cloud.system.dto.AdminTrendResponse;
import com.xueyifang.cloud.system.service.AdminStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    public AdminStatisticsController(AdminStatisticsService adminStatisticsService) {
        this.adminStatisticsService = adminStatisticsService;
    }

    @GetMapping
    public BaseResponse<AdminStatisticsResponse> getStatistics() {
        return ResultUtils.success(adminStatisticsService.getStatistics());
    }

    @GetMapping("/trend")
    public BaseResponse<AdminTrendResponse> getTrendStatistics() {
        return ResultUtils.success(adminStatisticsService.getTrendStatistics());
    }
}
