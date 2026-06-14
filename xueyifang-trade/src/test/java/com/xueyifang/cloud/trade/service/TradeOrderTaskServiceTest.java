package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.trade.config.TradeOrderTaskProperties;
import com.xueyifang.cloud.trade.dto.DisputeCreateRequest;
import com.xueyifang.cloud.trade.dto.OrderCreateRequest;
import com.xueyifang.cloud.trade.dto.OrderPayRequest;
import com.xueyifang.cloud.trade.dto.OrderRefundRequest;
import com.xueyifang.cloud.trade.dto.SellerHandleRefundRequest;
import com.xueyifang.cloud.trade.repository.TradeOrder;
import com.xueyifang.cloud.trade.repository.TradeServiceSnapshot;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.support.InMemoryTradeDisputeRepository;
import com.xueyifang.cloud.trade.support.InMemoryTradeOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TradeOrderTaskServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-21T00:00:00Z"), ZoneOffset.UTC);

    private final InMemoryTradeOrderRepository repository = new InMemoryTradeOrderRepository();

    private final InMemoryTradeDisputeRepository disputeRepository = new InMemoryTradeDisputeRepository(repository);

    private final TradeOrderService tradeOrderService = new TradeOrderService(repository, disputeRepository);

    private final TradeDisputeService tradeDisputeService =
            new TradeDisputeService(repository, disputeRepository, tradeOrderService);

    private final TradeOrderTaskProperties properties = new TradeOrderTaskProperties();

    private final TradeOrderTaskService tradeOrderTaskService =
            new TradeOrderTaskService(repository, tradeOrderService, properties, CLOCK);

    @BeforeEach
    void setUp() {
        repository.putService(new TradeServiceSnapshot(
                1L,
                10L,
                "Java tutoring",
                "Java help",
                new BigDecimal("20.00"),
                "java.jpg",
                1));
        repository.putUser(new TradeUserWallet(
                10L,
                1,
                "Seller",
                "seller.jpg",
                new BigDecimal("5.00"),
                BigDecimal.ZERO));
        repository.putUser(new TradeUserWallet(
                20L,
                1,
                "Buyer",
                "buyer.jpg",
                new BigDecimal("100.00"),
                BigDecimal.ZERO));
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void autoCancelsUnpaidOrdersAfterTimeout() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null));
        repository.setOrderTimes(orderId, LocalDateTime.parse("2026-06-19T23:59:00"), null, null);

        int processed = tradeOrderTaskService.autoCancelUnpaidOrders();

        assertThat(processed).isEqualTo(1);
        assertThat(repository.getOrder(orderId).orderStatus()).isEqualTo(5);
        assertThat(repository.logs()).extracting("actionType")
                .containsExactly("CREATE", "AUTO_CANCEL");
    }

    @Test
    void autoConfirmsReceiptAndSettlesFundsAfterTimeout() {
        Long orderId = createPaidAndShippedOrder();
        repository.setOrderTimes(orderId, repository.getOrder(orderId).createTime(),
                LocalDateTime.parse("2026-06-13T23:00:00"), null);

        int processed = tradeOrderTaskService.autoConfirmReceipt();

        TradeOrder order = repository.getOrder(orderId);
        assertThat(processed).isEqualTo(1);
        assertThat(order.orderStatus()).isEqualTo(4);
        assertThat(order.frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.getUser(20L).walletBalance()).isEqualByComparingTo("80.00");
        assertThat(repository.getUser(20L).frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.getUser(10L).walletBalance()).isEqualByComparingTo("25.00");
        assertThat(repository.transactions()).extracting("transactionType")
                .containsExactly(3, 6, 7, 5);
        assertThat(repository.logs()).extracting("actionType")
                .containsExactly("CREATE", "PAY", "SELLER_SHIP", "AUTO_CONFIRM_RECEIPT");
    }

    @Test
    void autoConfirmSkipsOrdersWithActiveDisputes() {
        Long orderId = createPaidAndShippedOrder();

        loginAsBuyer();
        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("not as described", null));
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.sellerHandleRefund(orderId, new SellerHandleRefundRequest(false, "will fix it"));
        loginAsBuyer();
        tradeDisputeService.createDispute(new DisputeCreateRequest(orderId, "seller refused refund", null));
        repository.setOrderTimes(orderId, repository.getOrder(orderId).createTime(),
                LocalDateTime.parse("2026-06-13T23:00:00"), repository.getOrder(orderId).refundRequestTime());

        int processed = tradeOrderTaskService.autoConfirmReceipt();

        assertThat(processed).isZero();
        assertThat(repository.getOrder(orderId).orderStatus()).isEqualTo(3);
        assertThat(repository.getUser(10L).walletBalance()).isEqualByComparingTo("5.00");
    }

    @Test
    void autoRefundsPendingRefundsAfterSellerTimeout() {
        Long orderId = createPaidAndShippedOrder();
        loginAsBuyer();
        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("not as described", null));
        repository.setOrderTimes(orderId, repository.getOrder(orderId).createTime(),
                repository.getOrder(orderId).sellerShipTime(), LocalDateTime.parse("2026-06-17T00:00:00"));

        int processed = tradeOrderTaskService.autoRefundTimeout();

        TradeOrder order = repository.getOrder(orderId);
        assertThat(processed).isEqualTo(1);
        assertThat(order.orderStatus()).isEqualTo(6);
        assertThat(order.paymentStatus()).isEqualTo(3);
        assertThat(order.refundStatus()).isEqualTo(4);
        assertThat(order.frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.getUser(20L).walletBalance()).isEqualByComparingTo("100.00");
        assertThat(repository.getUser(20L).frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.transactions()).extracting("transactionType")
                .containsExactly(3, 6, 7, 4);
        assertThat(repository.logs()).extracting("actionType")
                .containsExactly("CREATE", "PAY", "SELLER_SHIP", "REFUND_REQUEST", "AUTO_REFUND");
    }

    private Long createPaidAndShippedOrder() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null));
        tradeOrderService.payOrder(orderId, new OrderPayRequest(null, 1));
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.shipOrder(orderId);
        return orderId;
    }

    private void loginAsBuyer() {
        UserContextHolder.set(new LoginUserContext(20L, 1, 1));
    }
}
