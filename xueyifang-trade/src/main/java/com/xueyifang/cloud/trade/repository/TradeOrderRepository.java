package com.xueyifang.cloud.trade.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TradeOrderRepository {

    Optional<TradeServiceSnapshot> findServiceForOrder(Long serviceId);

    Optional<TradeUserWallet> findUserById(Long userId);

    Optional<TradeUserWallet> findUserForUpdate(Long userId);

    boolean updateUserWallet(Long userId, BigDecimal walletBalance, BigDecimal frozenAmount);

    Long createOrder(OrderCreateCommand command);

    Optional<TradeOrder> findOrderById(Long orderId);

    Optional<TradeOrder> findOrderForUpdate(Long orderId);

    OrderPage findOrders(OrderListQuery query);

    boolean markOrderPaid(Long orderId, BigDecimal frozenAmount, Integer paymentMethod, LocalDateTime paymentTime);

    boolean cancelOrder(Long orderId);

    boolean shipOrder(Long orderId, LocalDateTime sellerShipTime);

    boolean completeOrder(Long orderId, LocalDateTime buyerConfirmTime);

    boolean requestRefund(Long orderId, String reason, LocalDateTime refundRequestTime);

    boolean rejectRefund(Long orderId);

    boolean markOrderRefunded(Long orderId, String reason, LocalDateTime refundTime);

    boolean incrementServiceOrderCount(Long serviceId, Integer quantity);

    WalletTransactionPage findWalletTransactions(WalletTransactionQuery query);

    void insertOrderLog(OrderLogCommand command);

    void insertWalletTransaction(WalletTransactionCommand command);
}
