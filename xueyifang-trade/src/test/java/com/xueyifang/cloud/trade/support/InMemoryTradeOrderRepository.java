package com.xueyifang.cloud.trade.support;

import com.xueyifang.cloud.trade.repository.OrderCreateCommand;
import com.xueyifang.cloud.trade.repository.OrderListQuery;
import com.xueyifang.cloud.trade.repository.OrderLogCommand;
import com.xueyifang.cloud.trade.repository.OrderPage;
import com.xueyifang.cloud.trade.repository.TradeOrder;
import com.xueyifang.cloud.trade.repository.TradeOrderRepository;
import com.xueyifang.cloud.trade.repository.TradeServiceSnapshot;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.repository.WalletTransactionCommand;
import com.xueyifang.cloud.trade.repository.WalletTransactionItem;
import com.xueyifang.cloud.trade.repository.WalletTransactionPage;
import com.xueyifang.cloud.trade.repository.WalletTransactionQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTradeOrderRepository implements TradeOrderRepository {

    private final Map<Long, TradeServiceSnapshot> services = new HashMap<>();

    private final Map<Long, TradeUserWallet> users = new HashMap<>();

    private final Map<Long, TradeOrder> orders = new HashMap<>();

    private final Map<Long, Integer> serviceOrderCounts = new HashMap<>();

    private final List<OrderLogCommand> logs = new ArrayList<>();

    private final List<WalletTransactionCommand> transactions = new ArrayList<>();

    private long nextOrderId = 1L;

    public void putService(TradeServiceSnapshot service) {
        services.put(service.serviceId(), service);
        serviceOrderCounts.putIfAbsent(service.serviceId(), 0);
    }

    public void putUser(TradeUserWallet user) {
        users.put(user.userId(), user);
    }

    public TradeOrder getOrder(Long orderId) {
        return orders.get(orderId);
    }

    public TradeUserWallet getUser(Long userId) {
        return users.get(userId);
    }

    public int serviceOrderCount(Long serviceId) {
        return serviceOrderCounts.getOrDefault(serviceId, 0);
    }

    public List<OrderLogCommand> logs() {
        return logs;
    }

    public List<WalletTransactionCommand> transactions() {
        return transactions;
    }

    public void setOrderTimes(Long orderId, LocalDateTime createTime,
                              LocalDateTime sellerShipTime, LocalDateTime refundRequestTime) {
        TradeOrder order = orders.get(orderId);
        if (order == null) {
            return;
        }
        orders.put(orderId, new TradeOrder(
                order.id(),
                order.orderNumber(),
                order.serviceId(),
                order.buyerId(),
                order.sellerId(),
                order.quantity(),
                order.unitPrice(),
                order.totalAmount(),
                order.tradeType(),
                order.tradeLocationId(),
                order.paymentStatus(),
                order.orderStatus(),
                order.frozenAmount(),
                order.paymentMethod(),
                order.paymentTime(),
                sellerShipTime,
                order.buyerConfirmTime(),
                order.refundStatus(),
                order.refundReason(),
                refundRequestTime,
                order.remark(),
                createTime,
                order.updateTime(),
                order.serviceTitle(),
                order.serviceDescription(),
                order.serviceImage(),
                order.buyerName(),
                order.buyerAvatar(),
                order.sellerName(),
                order.sellerAvatar(),
                order.isReviewed()));
    }

    @Override
    public Optional<TradeServiceSnapshot> findServiceForOrder(Long serviceId) {
        return Optional.ofNullable(services.get(serviceId));
    }

    @Override
    public Optional<TradeUserWallet> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<TradeUserWallet> findUserForUpdate(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public boolean updateUserWallet(Long userId, BigDecimal walletBalance, BigDecimal frozenAmount) {
        TradeUserWallet user = users.get(userId);
        if (user == null) {
            return false;
        }
        users.put(userId, new TradeUserWallet(
                user.userId(),
                user.role(),
                user.displayName(),
                user.avatar(),
                walletBalance,
                frozenAmount));
        return true;
    }

    @Override
    public Long createOrder(OrderCreateCommand command) {
        Long orderId = nextOrderId++;
        TradeServiceSnapshot service = services.get(command.serviceId());
        TradeUserWallet buyer = users.get(command.buyerId());
        TradeUserWallet seller = users.get(command.sellerId());
        LocalDateTime now = LocalDateTime.parse("2026-06-14T00:00:00").plusSeconds(orderId);
        orders.put(orderId, new TradeOrder(
                orderId,
                command.orderNumber(),
                command.serviceId(),
                command.buyerId(),
                command.sellerId(),
                command.quantity(),
                command.unitPrice(),
                command.totalAmount(),
                command.tradeType(),
                command.tradeLocationId(),
                command.paymentStatus(),
                command.orderStatus(),
                command.frozenAmount(),
                command.paymentMethod(),
                null,
                null,
                null,
                command.refundStatus(),
                null,
                null,
                command.remark(),
                now,
                now,
                service == null ? null : service.title(),
                service == null ? null : service.description(),
                service == null ? null : service.coverImage(),
                buyer == null ? null : buyer.displayName(),
                buyer == null ? null : buyer.avatar(),
                seller == null ? null : seller.displayName(),
                seller == null ? null : seller.avatar(),
                false));
        return orderId;
    }

    @Override
    public Optional<TradeOrder> findOrderById(Long orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public Optional<TradeOrder> findOrderForUpdate(Long orderId) {
        return findOrderById(orderId);
    }

    @Override
    public OrderPage findOrders(OrderListQuery query) {
        List<TradeOrder> matched = orders.values().stream()
                .filter(order -> query.buyerId() == null || query.buyerId().equals(order.buyerId()))
                .filter(order -> query.sellerId() == null || query.sellerId().equals(order.sellerId()))
                .filter(order -> query.orderStatus() == null || query.orderStatus().equals(order.orderStatus()))
                .sorted(Comparator.comparing(TradeOrder::createTime).reversed())
                .toList();
        List<TradeOrder> records = matched.stream()
                .skip(query.offset())
                .limit(query.limit())
                .toList();
        return new OrderPage(records, matched.size());
    }

    @Override
    public List<Long> findUnpaidOrderIdsCreatedAtOrBefore(LocalDateTime deadline, int limit) {
        return orders.values().stream()
                .filter(order -> Integer.valueOf(1).equals(order.orderStatus()))
                .filter(order -> Integer.valueOf(1).equals(order.paymentStatus()))
                .filter(order -> !order.createTime().isAfter(deadline))
                .sorted(Comparator.comparing(TradeOrder::createTime).thenComparing(TradeOrder::id))
                .limit(limit)
                .map(TradeOrder::id)
                .toList();
    }

    @Override
    public List<Long> findPendingReceiptOrderIdsShippedAtOrBefore(LocalDateTime deadline, int limit) {
        return orders.values().stream()
                .filter(order -> Integer.valueOf(3).equals(order.orderStatus()))
                .filter(order -> Integer.valueOf(2).equals(order.paymentStatus()))
                .filter(order -> order.sellerShipTime() != null && !order.sellerShipTime().isAfter(deadline))
                .filter(order -> !Integer.valueOf(1).equals(order.refundStatus()))
                .sorted(Comparator.comparing(TradeOrder::sellerShipTime).thenComparing(TradeOrder::id))
                .limit(limit)
                .map(TradeOrder::id)
                .toList();
    }

    @Override
    public List<Long> findPendingRefundOrderIdsRequestedAtOrBefore(LocalDateTime deadline, int limit) {
        return orders.values().stream()
                .filter(order -> Integer.valueOf(1).equals(order.refundStatus()))
                .filter(order -> Integer.valueOf(2).equals(order.paymentStatus()))
                .filter(order -> Integer.valueOf(2).equals(order.orderStatus())
                        || Integer.valueOf(3).equals(order.orderStatus()))
                .filter(order -> order.refundRequestTime() != null && !order.refundRequestTime().isAfter(deadline))
                .sorted(Comparator.comparing(TradeOrder::refundRequestTime).thenComparing(TradeOrder::id))
                .limit(limit)
                .map(TradeOrder::id)
                .toList();
    }

    @Override
    public boolean markOrderPaid(Long orderId, BigDecimal frozenAmount, Integer paymentMethod, LocalDateTime paymentTime) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(1).equals(order.orderStatus())
                || !Integer.valueOf(1).equals(order.paymentStatus())) {
            return false;
        }
        orders.put(orderId, copy(order, 2, 2, frozenAmount, paymentMethod, paymentTime,
                order.sellerShipTime(), order.buyerConfirmTime()));
        return true;
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(1).equals(order.orderStatus())) {
            return false;
        }
        orders.put(orderId, copy(order, 5, order.paymentStatus(), order.frozenAmount(), order.paymentMethod(),
                order.paymentTime(), order.sellerShipTime(), order.buyerConfirmTime()));
        return true;
    }

    @Override
    public boolean shipOrder(Long orderId, LocalDateTime sellerShipTime) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(2).equals(order.orderStatus())
                || Integer.valueOf(1).equals(order.refundStatus())) {
            return false;
        }
        orders.put(orderId, copy(order, 3, order.paymentStatus(), order.frozenAmount(), order.paymentMethod(),
                order.paymentTime(), sellerShipTime, order.buyerConfirmTime()));
        return true;
    }

    @Override
    public boolean completeOrder(Long orderId, LocalDateTime buyerConfirmTime) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(3).equals(order.orderStatus())
                || Integer.valueOf(1).equals(order.refundStatus())) {
            return false;
        }
        orders.put(orderId, copy(order, 4, order.paymentStatus(), BigDecimal.ZERO, order.paymentMethod(),
                order.paymentTime(), order.sellerShipTime(), buyerConfirmTime, 0,
                order.refundReason(), order.refundRequestTime()));
        return true;
    }

    @Override
    public boolean requestRefund(Long orderId, String reason, LocalDateTime refundRequestTime) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(3).equals(order.orderStatus())
                || !Integer.valueOf(2).equals(order.paymentStatus())
                || !(Integer.valueOf(0).equals(order.refundStatus()) || Integer.valueOf(3).equals(order.refundStatus()))) {
            return false;
        }
        orders.put(orderId, copy(order, order.orderStatus(), order.paymentStatus(), order.frozenAmount(),
                order.paymentMethod(), order.paymentTime(), order.sellerShipTime(), order.buyerConfirmTime(),
                1, reason, refundRequestTime));
        return true;
    }

    @Override
    public boolean rejectRefund(Long orderId) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(1).equals(order.refundStatus())) {
            return false;
        }
        orders.put(orderId, copy(order, order.orderStatus(), order.paymentStatus(), order.frozenAmount(),
                order.paymentMethod(), order.paymentTime(), order.sellerShipTime(), order.buyerConfirmTime(),
                3, order.refundReason(), order.refundRequestTime()));
        return true;
    }

    @Override
    public boolean markOrderRefunded(Long orderId, String reason, LocalDateTime refundTime) {
        TradeOrder order = orders.get(orderId);
        if (order == null || !Integer.valueOf(2).equals(order.paymentStatus())
                || !(Integer.valueOf(2).equals(order.orderStatus()) || Integer.valueOf(3).equals(order.orderStatus()))
                || order.frozenAmount().signum() <= 0
                || Integer.valueOf(4).equals(order.refundStatus())) {
            return false;
        }
        orders.put(orderId, copy(order, 6, 3, BigDecimal.ZERO, order.paymentMethod(),
                order.paymentTime(), order.sellerShipTime(), order.buyerConfirmTime(), 4,
                reason, order.refundRequestTime() == null ? refundTime : order.refundRequestTime()));
        return true;
    }

    @Override
    public boolean incrementServiceOrderCount(Long serviceId, Integer quantity) {
        if (!services.containsKey(serviceId)) {
            return false;
        }
        serviceOrderCounts.compute(serviceId, (id, count) -> (count == null ? 0 : count) + quantity);
        return true;
    }

    @Override
    public WalletTransactionPage findWalletTransactions(WalletTransactionQuery query) {
        List<WalletTransactionItem> items = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.parse("2026-06-14T02:00:00");
        for (int i = 0; i < transactions.size(); i++) {
            WalletTransactionCommand transaction = transactions.get(i);
            LocalDateTime createTime = baseTime.plusSeconds(i + 1L);
            TradeOrder order = transaction.relatedOrderId() == null ? null : orders.get(transaction.relatedOrderId());
            items.add(new WalletTransactionItem(
                    (long) i + 1,
                    transaction.userId(),
                    transaction.transactionType(),
                    transaction.amount(),
                    transaction.balanceBefore(),
                    transaction.balanceAfter(),
                    transaction.frozenBefore(),
                    transaction.frozenAfter(),
                    transaction.relatedOrderId(),
                    order == null ? null : order.orderNumber(),
                    transaction.transactionNo(),
                    transaction.remark(),
                    createTime));
        }

        List<WalletTransactionItem> matched = items.stream()
                .filter(item -> query.userId().equals(item.userId()))
                .filter(item -> query.transactionType() == null || query.transactionType().equals(item.transactionType()))
                .filter(item -> query.startTime() == null || !item.createTime().isBefore(query.startTime()))
                .filter(item -> query.endTime() == null || !item.createTime().isAfter(query.endTime()))
                .sorted(Comparator.comparing(WalletTransactionItem::createTime).reversed())
                .toList();
        List<WalletTransactionItem> records = matched.stream()
                .skip(query.offset())
                .limit(query.limit())
                .toList();
        return new WalletTransactionPage(records, matched.size());
    }

    @Override
    public void insertOrderLog(OrderLogCommand command) {
        logs.add(command);
    }

    @Override
    public void insertWalletTransaction(WalletTransactionCommand command) {
        transactions.add(command);
    }

    private TradeOrder copy(TradeOrder order, Integer orderStatus, Integer paymentStatus,
                            BigDecimal frozenAmount, Integer paymentMethod, LocalDateTime paymentTime,
                            LocalDateTime sellerShipTime, LocalDateTime buyerConfirmTime) {
        return copy(order, orderStatus, paymentStatus, frozenAmount, paymentMethod, paymentTime,
                sellerShipTime, buyerConfirmTime, order.refundStatus(), order.refundReason(),
                order.refundRequestTime());
    }

    private TradeOrder copy(TradeOrder order, Integer orderStatus, Integer paymentStatus,
                            BigDecimal frozenAmount, Integer paymentMethod, LocalDateTime paymentTime,
                            LocalDateTime sellerShipTime, LocalDateTime buyerConfirmTime,
                            Integer refundStatus, String refundReason, LocalDateTime refundRequestTime) {
        return new TradeOrder(
                order.id(),
                order.orderNumber(),
                order.serviceId(),
                order.buyerId(),
                order.sellerId(),
                order.quantity(),
                order.unitPrice(),
                order.totalAmount(),
                order.tradeType(),
                order.tradeLocationId(),
                paymentStatus,
                orderStatus,
                frozenAmount,
                paymentMethod,
                paymentTime,
                sellerShipTime,
                buyerConfirmTime,
                refundStatus,
                refundReason,
                refundRequestTime,
                order.remark(),
                order.createTime(),
                LocalDateTime.parse("2026-06-14T01:00:00"),
                order.serviceTitle(),
                order.serviceDescription(),
                order.serviceImage(),
                order.buyerName(),
                order.buyerAvatar(),
                order.sellerName(),
                order.sellerAvatar(),
                order.isReviewed());
    }
}
