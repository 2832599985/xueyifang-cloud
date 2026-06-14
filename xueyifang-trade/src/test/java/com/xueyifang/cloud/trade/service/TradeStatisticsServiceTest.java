package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.UserSalesStatisticsResponse;
import com.xueyifang.cloud.trade.repository.OrderCreateCommand;
import com.xueyifang.cloud.trade.repository.TradeServiceSnapshot;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.support.InMemoryTradeOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeStatisticsServiceTest {

    private final InMemoryTradeOrderRepository repository = new InMemoryTradeOrderRepository();

    private final TradeStatisticsService service = new TradeStatisticsService(repository);

    @BeforeEach
    void setUp() {
        repository.putUser(new TradeUserWallet(10L, 1, "Buyer", null, BigDecimal.ZERO, BigDecimal.ZERO));
        repository.putUser(new TradeUserWallet(20L, 1, "Seller", null, BigDecimal.ZERO, BigDecimal.ZERO));
        repository.putUser(new TradeUserWallet(30L, 1, "Other", null, BigDecimal.ZERO, BigDecimal.ZERO));
        repository.putService(new TradeServiceSnapshot(
                100L, 20L, "Math Tutoring", "desc", new BigDecimal("50.00"), null, 1));
        repository.putService(new TradeServiceSnapshot(
                200L, 20L, "Design Help", "desc", new BigDecimal("70.00"), null, 1));

        createCompletedOrder("O-1", 100L, 10L, 20L, "50.00");
        createCompletedOrder("O-2", 100L, 10L, 20L, "50.00");
        createCompletedOrder("O-3", 200L, 10L, 20L, "70.00");
        createCompletedOrder("O-4", 200L, 10L, 30L, "70.00");
        UserContextHolder.set(new LoginUserContext(20L, 1, 1));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void summarizesCurrentSellerCompletedOrders() {
        UserSalesStatisticsResponse response = service.getCurrentUserSalesStatistics();

        assertThat(response.totalSales()).isEqualTo(3);
        assertThat(response.totalRevenue()).isEqualByComparingTo("170.00");
        assertThat(response.averagePrice()).isEqualByComparingTo("56.67");
        assertThat(response.bestService().serviceId()).isEqualTo(100L);
        assertThat(response.bestService().sales()).isEqualTo(2);
        assertThat(response.recentOrders()).extracting("orderNumber")
                .containsExactly("O-3", "O-2", "O-1");
    }

    @Test
    void rejectsMissingUser() {
        UserContextHolder.set(new LoginUserContext(404L, 1, 1));

        assertThatThrownBy(service::getCurrentUserSalesStatistics)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_EXIST.getCode()));
    }

    private void createCompletedOrder(String orderNumber, Long serviceId, Long buyerId, Long sellerId,
                                      String totalAmount) {
        repository.createOrder(new OrderCreateCommand(
                orderNumber,
                serviceId,
                buyerId,
                sellerId,
                1,
                new BigDecimal(totalAmount),
                new BigDecimal(totalAmount),
                1,
                null,
                2,
                4,
                BigDecimal.ZERO,
                1,
                0,
                null));
    }
}
