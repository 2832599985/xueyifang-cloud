package com.xueyifang.cloud.system.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.system.dto.AdminStatisticsResponse;
import com.xueyifang.cloud.system.dto.AdminTrendResponse;
import com.xueyifang.cloud.system.repository.SystemStatisticsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminStatisticsService {

    private static final int ADMIN_ROLE = 2;

    private static final int TREND_DAYS = 7;

    private static final DateTimeFormatter TREND_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final SystemStatisticsRepository repository;

    public AdminStatisticsService(SystemStatisticsRepository repository) {
        this.repository = repository;
    }

    public AdminStatisticsResponse getStatistics() {
        requireAdmin();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime activeSince = LocalDateTime.now().minusDays(30);

        return new AdminStatisticsResponse(
                toInt(repository.countUsers()),
                toInt(repository.countActiveUsersSince(activeSince)),
                toInt(repository.countServices()),
                toInt(repository.countOrders()),
                toInt(repository.countCompletedOrders()),
                toInt(repository.countPendingDisputes()),
                repository.sumCompletedOrderAmount(),
                toInt(repository.countUsersCreatedBetween(startOfToday, startOfTomorrow)),
                toInt(repository.countServicesCreatedBetween(startOfToday, startOfTomorrow)),
                toInt(repository.countOrdersCreatedBetween(startOfToday, startOfTomorrow)));
    }

    public AdminTrendResponse getTrendStatistics() {
        requireAdmin();

        List<String> dates = new ArrayList<>(TREND_DAYS);
        List<Integer> orderCounts = new ArrayList<>(TREND_DAYS);
        List<Integer> serviceCounts = new ArrayList<>(TREND_DAYS);
        List<BigDecimal> transactionAmounts = new ArrayList<>(TREND_DAYS);

        LocalDate today = LocalDate.now();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            dates.add(date.format(TREND_DATE_FORMATTER));
            orderCounts.add(toInt(repository.countOrdersCreatedBetween(start, end)));
            serviceCounts.add(toInt(repository.countServicesCreatedBetween(start, end)));
            transactionAmounts.add(repository.sumCompletedOrderAmountBetween(start, end));
        }

        return new AdminTrendResponse(dates, orderCounts, serviceCounts, transactionAmounts);
    }

    private LoginUserContext requireAdmin() {
        LoginUserContext user = UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
        if (!Integer.valueOf(ADMIN_ROLE).equals(user.role())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "admin role required");
        }
        return user;
    }

    private Integer toInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
