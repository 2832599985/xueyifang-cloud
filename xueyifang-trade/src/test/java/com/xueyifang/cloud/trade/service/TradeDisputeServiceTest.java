package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.DisputeCreateRequest;
import com.xueyifang.cloud.trade.dto.DisputeHandleRequest;
import com.xueyifang.cloud.trade.dto.DisputeListResponse;
import com.xueyifang.cloud.trade.dto.DisputeResponse;
import com.xueyifang.cloud.trade.dto.OrderCreateRequest;
import com.xueyifang.cloud.trade.dto.OrderPayRequest;
import com.xueyifang.cloud.trade.dto.OrderRefundRequest;
import com.xueyifang.cloud.trade.dto.SellerHandleRefundRequest;
import com.xueyifang.cloud.trade.repository.TradeDispute;
import com.xueyifang.cloud.trade.repository.TradeOrder;
import com.xueyifang.cloud.trade.repository.TradeServiceSnapshot;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.support.InMemoryTradeDisputeRepository;
import com.xueyifang.cloud.trade.support.InMemoryTradeOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeDisputeServiceTest {

    private final InMemoryTradeOrderRepository orderRepository = new InMemoryTradeOrderRepository();

    private final InMemoryTradeDisputeRepository disputeRepository =
            new InMemoryTradeDisputeRepository(orderRepository);

    private final TradeOrderService tradeOrderService =
            new TradeOrderService(orderRepository, disputeRepository);

    private final TradeDisputeService tradeDisputeService =
            new TradeDisputeService(orderRepository, disputeRepository, tradeOrderService);

    @BeforeEach
    void setUp() {
        orderRepository.putService(new TradeServiceSnapshot(
                1L,
                10L,
                "Java tutoring",
                "Java help",
                new BigDecimal("20.00"),
                "java.jpg",
                1));
        orderRepository.putUser(new TradeUserWallet(
                10L,
                1,
                "Seller",
                "seller.jpg",
                new BigDecimal("5.00"),
                BigDecimal.ZERO));
        orderRepository.putUser(new TradeUserWallet(
                20L,
                1,
                "Buyer",
                "buyer.jpg",
                new BigDecimal("100.00"),
                BigDecimal.ZERO));
        orderRepository.putUser(new TradeUserWallet(
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
    void createsDisputeAfterRejectedRefundAndBlocksBuyerConfirm() {
        Long orderId = createRejectedRefundOrder();

        loginAsBuyer();
        Long disputeId = tradeDisputeService.createDispute(new DisputeCreateRequest(
                orderId, "seller refused refund", "image-1"));

        TradeDispute dispute = disputeRepository.getDispute(disputeId);
        assertThat(dispute.orderId()).isEqualTo(orderId);
        assertThat(dispute.complainantId()).isEqualTo(20L);
        assertThat(dispute.respondentId()).isEqualTo(10L);
        assertThat(dispute.status()).isEqualTo(1);
        assertThat(dispute.evidence()).isEqualTo("image-1");

        DisputeListResponse myDisputes = tradeDisputeService.listMyDisputes(1, 10, null);
        assertThat(myDisputes.total()).isEqualTo(1);
        assertThat(myDisputes.records().getFirst().serviceTitle()).isEqualTo("Java tutoring");

        DisputeResponse detail = tradeDisputeService.getDisputeDetail(disputeId);
        assertThat(detail.buyerName()).isEqualTo("Buyer");
        assertThat(detail.sellerName()).isEqualTo("Seller");

        assertThatThrownBy(() -> tradeOrderService.confirmOrder(orderId))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.OPERATION_ERROR.getCode()));
    }

    @Test
    void adminApprovesDisputeAndRefundsOrder() {
        Long orderId = createRejectedRefundOrder();
        loginAsBuyer();
        Long disputeId = tradeDisputeService.createDispute(new DisputeCreateRequest(
                orderId, "seller refused refund", null));

        loginAsAdmin();
        tradeDisputeService.handleDispute(disputeId, new DisputeHandleRequest(true, "buyer evidence is valid"));

        TradeDispute dispute = disputeRepository.getDispute(disputeId);
        TradeOrder order = orderRepository.getOrder(orderId);
        assertThat(dispute.status()).isEqualTo(2);
        assertThat(dispute.handleResult()).isEqualTo("REFUND_APPROVED");
        assertThat(order.orderStatus()).isEqualTo(6);
        assertThat(order.paymentStatus()).isEqualTo(3);
        assertThat(order.refundStatus()).isEqualTo(4);
        assertThat(orderRepository.getUser(20L).walletBalance()).isEqualByComparingTo("100.00");
        assertThat(orderRepository.getUser(20L).frozenAmount()).isEqualByComparingTo("0.00");

        DisputeListResponse handled = tradeDisputeService.listAdminDisputes(1, 10, 2);
        assertThat(handled.total()).isEqualTo(1);
        assertThat(orderRepository.logs()).extracting("actionType")
                .contains("ADMIN_REFUND", "DISPUTE_REFUND");
    }

    @Test
    void adminRejectsDisputeAndBuyerCanConfirmOrderAfterwards() {
        Long orderId = createRejectedRefundOrder();
        loginAsBuyer();
        Long disputeId = tradeDisputeService.createDispute(new DisputeCreateRequest(
                orderId, "seller refused refund", null));

        loginAsAdmin();
        tradeDisputeService.handleDispute(disputeId, new DisputeHandleRequest(false, "service was delivered"));

        TradeDispute dispute = disputeRepository.getDispute(disputeId);
        assertThat(dispute.status()).isEqualTo(3);
        assertThat(dispute.handleResult()).isEqualTo("REJECTED");
        assertThat(orderRepository.getOrder(orderId).orderStatus()).isEqualTo(3);

        loginAsBuyer();
        tradeOrderService.confirmOrder(orderId);
        assertThat(orderRepository.getOrder(orderId).orderStatus()).isEqualTo(4);
        assertThat(orderRepository.getUser(10L).walletBalance()).isEqualByComparingTo("25.00");
    }

    @Test
    void rejectsDisputeBeforeRefundIsRejectedOrByNonBuyer() {
        Long orderId = createShippedOrder();

        loginAsBuyer();
        assertThatThrownBy(() -> tradeDisputeService.createDispute(new DisputeCreateRequest(
                orderId, "not ready", null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISPUTE_STATUS_ERROR.getCode()));

        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("not as described", null));
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.sellerHandleRefund(orderId, new SellerHandleRefundRequest(false, "will fix it"));

        UserContextHolder.set(new LoginUserContext(30L, 1, 1));
        assertThatThrownBy(() -> tradeDisputeService.createDispute(new DisputeCreateRequest(
                orderId, "not my order", null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    private Long createRejectedRefundOrder() {
        Long orderId = createShippedOrder();
        loginAsBuyer();
        tradeOrderService.requestRefund(orderId, new OrderRefundRequest("not as described", null));
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        tradeOrderService.sellerHandleRefund(orderId, new SellerHandleRefundRequest(false, "will fix it"));
        return orderId;
    }

    private Long createShippedOrder() {
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

    private void loginAsAdmin() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));
    }
}
