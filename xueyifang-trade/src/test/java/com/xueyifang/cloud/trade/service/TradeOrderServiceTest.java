package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.OrderCreateRequest;
import com.xueyifang.cloud.trade.dto.OrderDetailResponse;
import com.xueyifang.cloud.trade.dto.OrderListResponse;
import com.xueyifang.cloud.trade.dto.OrderPayRequest;
import com.xueyifang.cloud.trade.dto.OrderRefundRequest;
import com.xueyifang.cloud.trade.dto.SellerHandleRefundRequest;
import com.xueyifang.cloud.trade.repository.TradeOrder;
import com.xueyifang.cloud.trade.repository.TradeServiceSnapshot;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.support.InMemoryTradeDisputeRepository;
import com.xueyifang.cloud.trade.support.InMemoryTradeOrderRepository;
import com.xueyifang.cloud.trade.support.RecordingTradeNotificationPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeOrderServiceTest {

    private final InMemoryTradeOrderRepository repository = new InMemoryTradeOrderRepository();

    private final InMemoryTradeDisputeRepository disputeRepository = new InMemoryTradeDisputeRepository(repository);

    private final RecordingTradeNotificationPublisher notificationPublisher =
            new RecordingTradeNotificationPublisher();

    private final TradeOrderService tradeOrderService =
            new TradeOrderService(repository, disputeRepository, notificationPublisher);

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
        repository.putService(new TradeServiceSnapshot(
                2L,
                11L,
                "Offline service",
                "hidden",
                new BigDecimal("12.00"),
                null,
                0));
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
        repository.putUser(new TradeUserWallet(
                30L,
                1,
                "Other",
                null,
                new BigDecimal("50.00"),
                BigDecimal.ZERO));
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void createsOrderForOnlineService() {
        loginAsBuyer();

        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 2, 2, null, "after class"));

        TradeOrder order = repository.getOrder(orderId);
        assertThat(order.buyerId()).isEqualTo(20L);
        assertThat(order.sellerId()).isEqualTo(10L);
        assertThat(order.totalAmount()).isEqualByComparingTo("40.00");
        assertThat(order.orderStatus()).isEqualTo(1);
        assertThat(order.paymentStatus()).isEqualTo(1);
        assertThat(order.remark()).isEqualTo("after class");
        assertThat(repository.logs()).extracting("actionType").containsExactly("CREATE");
        assertThat(notificationPublisher.notifications()).hasSize(1);
        assertThat(notificationPublisher.notifications().getFirst().recipientId()).isEqualTo(10L);
        assertThat(notificationPublisher.notifications().getFirst().relatedId()).isEqualTo(orderId);
    }

    @Test
    void rejectsInvalidCreateCases() {
        loginAsBuyer();

        assertThatThrownBy(() -> tradeOrderService.createOrder(new OrderCreateRequest(2L, 1, 2, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_OFFLINE.getCode()));

        assertThatThrownBy(() -> tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 1, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));

        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        assertThatThrownBy(() -> tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.OPERATION_ERROR.getCode()));
    }

    @Test
    void paysOrderAndFreezesBuyerFunds() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 2, 2, null, null));

        tradeOrderService.payOrder(orderId, new OrderPayRequest(null, null));

        TradeOrder order = repository.getOrder(orderId);
        TradeUserWallet buyer = repository.getUser(20L);
        assertThat(order.orderStatus()).isEqualTo(2);
        assertThat(order.paymentStatus()).isEqualTo(2);
        assertThat(order.frozenAmount()).isEqualByComparingTo("40.00");
        assertThat(buyer.walletBalance()).isEqualByComparingTo("60.00");
        assertThat(buyer.frozenAmount()).isEqualByComparingTo("40.00");
        assertThat(repository.serviceOrderCount(1L)).isEqualTo(2);
        assertThat(repository.transactions()).extracting("transactionType").containsExactly(3, 6);
        assertThat(repository.logs()).extracting("actionType").containsExactly("CREATE", "PAY");
        assertThat(notificationPublisher.notifications()).extracting("title")
                .containsExactly("收到新订单", "订单已支付");
    }

    @Test
    void rejectsPaymentWhenBalanceIsInsufficient() {
        repository.putUser(new TradeUserWallet(
                20L,
                1,
                "Buyer",
                "buyer.jpg",
                new BigDecimal("10.00"),
                BigDecimal.ZERO));
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null));

        assertThatThrownBy(() -> tradeOrderService.payOrder(orderId, new OrderPayRequest(null, 1)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.ORDER_BALANCE_NOT_ENOUGH.getCode()));
    }

    @Test
    void sellerShipsAndBuyerConfirmsOrder() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 2, 2, null, null));
        tradeOrderService.payOrder(orderId, new OrderPayRequest(null, 1));

        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.shipOrder(orderId);
        assertThat(repository.getOrder(orderId).orderStatus()).isEqualTo(3);

        loginAsBuyer();
        tradeOrderService.confirmOrder(orderId);

        TradeOrder order = repository.getOrder(orderId);
        assertThat(order.orderStatus()).isEqualTo(4);
        assertThat(order.frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.getUser(20L).walletBalance()).isEqualByComparingTo("60.00");
        assertThat(repository.getUser(20L).frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.getUser(10L).walletBalance()).isEqualByComparingTo("45.00");
        assertThat(repository.transactions()).extracting("transactionType").containsExactly(3, 6, 7, 5);
        assertThat(repository.logs()).extracting("actionType")
                .containsExactly("CREATE", "PAY", "SELLER_SHIP", "BUYER_CONFIRM");
        assertThat(notificationPublisher.notifications()).extracting("recipientId")
                .containsExactly(10L, 10L, 20L, 10L);
    }

    @Test
    void cancelsOnlyBuyerUnpaidOrder() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null));

        UserContextHolder.set(new LoginUserContext(30L, 1, 1));
        assertThatThrownBy(() -> tradeOrderService.cancelOrder(orderId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));

        loginAsBuyer();
        tradeOrderService.cancelOrder(orderId);
        assertThat(repository.getOrder(orderId).orderStatus()).isEqualTo(5);
    }

    @Test
    void directlyRefundsPaidOrderBeforeShipment() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 2, 2, null, null));
        tradeOrderService.payOrder(orderId, new OrderPayRequest(null, 1));

        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("seller has not shipped", null));

        TradeOrder order = repository.getOrder(orderId);
        assertThat(order.orderStatus()).isEqualTo(6);
        assertThat(order.paymentStatus()).isEqualTo(3);
        assertThat(order.refundStatus()).isEqualTo(4);
        assertThat(order.frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.getUser(20L).walletBalance()).isEqualByComparingTo("100.00");
        assertThat(repository.getUser(20L).frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.transactions()).extracting("transactionType").containsExactly(3, 6, 7, 4);
        assertThat(repository.logs()).extracting("actionType").containsExactly("CREATE", "PAY", "REFUND");

        assertThatThrownBy(() -> tradeOrderService.requestRefund(orderId, new OrderRefundRequest("again", null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.ORDER_STATUS_ERROR.getCode()));
    }

    @Test
    void requestsAndSellerHandlesRefundAfterShipment() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null));
        tradeOrderService.payOrder(orderId, new OrderPayRequest(null, 1));

        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.shipOrder(orderId);

        loginAsBuyer();
        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("not as described", null));

        TradeOrder pendingRefund = repository.getOrder(orderId);
        assertThat(pendingRefund.orderStatus()).isEqualTo(3);
        assertThat(pendingRefund.refundStatus()).isEqualTo(1);
        assertThatThrownBy(() -> tradeOrderService.confirmOrder(orderId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.OPERATION_ERROR.getCode()));

        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.sellerHandleRefund(orderId, new SellerHandleRefundRequest(false, "will fix it"));
        assertThat(repository.getOrder(orderId).refundStatus()).isEqualTo(3);

        loginAsBuyer();
        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("still not fixed", null));
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.sellerHandleRefund(orderId, new SellerHandleRefundRequest(true, null));

        TradeOrder refunded = repository.getOrder(orderId);
        assertThat(refunded.orderStatus()).isEqualTo(6);
        assertThat(refunded.paymentStatus()).isEqualTo(3);
        assertThat(refunded.refundStatus()).isEqualTo(4);
        assertThat(repository.getUser(20L).walletBalance()).isEqualByComparingTo("100.00");
        assertThat(repository.getUser(20L).frozenAmount()).isEqualByComparingTo("0.00");
        assertThat(repository.transactions()).extracting("transactionType").containsExactly(3, 6, 7, 4);
        assertThat(repository.logs()).extracting("actionType")
                .containsExactly("CREATE", "PAY", "SELLER_SHIP", "REFUND_REQUEST",
                        "SELLER_REJECT_REFUND", "REFUND_REQUEST", "SELLER_APPROVE_REFUND");
    }

    @Test
    void listsBuyerAndSellerOrdersAndProtectsDetail() {
        loginAsBuyer();
        Long orderId = tradeOrderService.createOrder(new OrderCreateRequest(1L, 1, 2, null, null));
        tradeOrderService.payOrder(orderId, new OrderPayRequest(null, 1));

        OrderListResponse buyerOrders = tradeOrderService.listMyOrders(1, 10, null);
        assertThat(buyerOrders.total()).isEqualTo(1);
        assertThat(buyerOrders.records().getFirst().serviceTitle()).isEqualTo("Java tutoring");

        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        OrderListResponse sellerOrders = tradeOrderService.listMySellingOrders(1, 10, 2);
        assertThat(sellerOrders.total()).isEqualTo(1);

        OrderDetailResponse sellerDetail = tradeOrderService.getOrderDetail(orderId);
        assertThat(sellerDetail.buyerName()).isEqualTo("Buyer");
        assertThat(sellerDetail.sellerName()).isEqualTo("Seller");

        UserContextHolder.set(new LoginUserContext(30L, 1, 1));
        assertThatThrownBy(() -> tradeOrderService.getOrderDetail(orderId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    private void loginAsBuyer() {
        UserContextHolder.set(new LoginUserContext(20L, 1, 1));
    }
}
