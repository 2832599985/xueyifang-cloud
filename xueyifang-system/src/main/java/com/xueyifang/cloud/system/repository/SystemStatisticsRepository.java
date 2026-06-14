package com.xueyifang.cloud.system.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SystemStatisticsRepository {

    long countUsers();

    long countActiveUsersSince(LocalDateTime since);

    long countServices();

    long countOrders();

    long countCompletedOrders();

    long countPendingDisputes();

    BigDecimal sumCompletedOrderAmount();

    long countUsersCreatedBetween(LocalDateTime start, LocalDateTime end);

    long countServicesCreatedBetween(LocalDateTime start, LocalDateTime end);

    long countOrdersCreatedBetween(LocalDateTime start, LocalDateTime end);

    BigDecimal sumCompletedOrderAmountBetween(LocalDateTime start, LocalDateTime end);
}
