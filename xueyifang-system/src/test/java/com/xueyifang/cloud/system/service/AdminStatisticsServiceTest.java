package com.xueyifang.cloud.system.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.system.dto.AdminStatisticsResponse;
import com.xueyifang.cloud.system.dto.AdminTrendResponse;
import com.xueyifang.cloud.system.repository.SystemStatisticsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminStatisticsServiceTest {

    private final FakeSystemStatisticsRepository repository = new FakeSystemStatisticsRepository();

    private final AdminStatisticsService service = new AdminStatisticsService(repository);

    @BeforeEach
    void setUp() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void returnsAdminStatisticsAndSevenDayTrend() {
        AdminStatisticsResponse statistics = service.getStatistics();
        AdminTrendResponse trend = service.getTrendStatistics();

        assertThat(statistics.totalUsers()).isEqualTo(10);
        assertThat(statistics.activeUsers()).isEqualTo(4);
        assertThat(statistics.totalTransactionAmount()).isEqualByComparingTo("120.50");
        assertThat(trend.dates()).hasSize(7);
        assertThat(trend.orderCounts()).containsOnly(2);
        assertThat(trend.serviceCounts()).containsOnly(1);
        assertThat(trend.transactionAmounts()).containsOnly(new BigDecimal("30.00"));
    }

    @Test
    void rejectsStatisticsForNormalUser() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        assertThatThrownBy(service::getStatistics)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    private static class FakeSystemStatisticsRepository implements SystemStatisticsRepository {

        @Override
        public long countUsers() {
            return 10;
        }

        @Override
        public long countActiveUsersSince(LocalDateTime since) {
            return 4;
        }

        @Override
        public long countServices() {
            return 5;
        }

        @Override
        public long countOrders() {
            return 8;
        }

        @Override
        public long countCompletedOrders() {
            return 6;
        }

        @Override
        public long countPendingDisputes() {
            return 1;
        }

        @Override
        public BigDecimal sumCompletedOrderAmount() {
            return new BigDecimal("120.50");
        }

        @Override
        public long countUsersCreatedBetween(LocalDateTime start, LocalDateTime end) {
            return 1;
        }

        @Override
        public long countServicesCreatedBetween(LocalDateTime start, LocalDateTime end) {
            return 1;
        }

        @Override
        public long countOrdersCreatedBetween(LocalDateTime start, LocalDateTime end) {
            return 2;
        }

        @Override
        public BigDecimal sumCompletedOrderAmountBetween(LocalDateTime start, LocalDateTime end) {
            return new BigDecimal("30.00");
        }
    }
}
