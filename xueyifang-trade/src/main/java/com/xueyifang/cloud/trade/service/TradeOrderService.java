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
import com.xueyifang.cloud.trade.notification.TradeNotificationPublisher;
import com.xueyifang.cloud.trade.repository.OrderCreateCommand;
import com.xueyifang.cloud.trade.repository.OrderListQuery;
import com.xueyifang.cloud.trade.repository.OrderLogCommand;
import com.xueyifang.cloud.trade.repository.TradeDisputeRepository;
import com.xueyifang.cloud.trade.repository.TradeOrder;
import com.xueyifang.cloud.trade.repository.TradeOrderRepository;
import com.xueyifang.cloud.trade.repository.TradeServiceSnapshot;
import com.xueyifang.cloud.trade.repository.TradeUserWallet;
import com.xueyifang.cloud.trade.repository.WalletTransactionCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TradeOrderService {

    private static final int SERVICE_ONLINE = 1;

    private static final int ORDER_UNPAID = 1;

    private static final int ORDER_PENDING_SHIP = 2;

    private static final int ORDER_PENDING_RECEIPT = 3;

    private static final int ORDER_COMPLETED = 4;

    private static final int ORDER_CANCELLED = 5;

    private static final int ORDER_FAILED = 6;

    private static final int PAYMENT_UNPAID = 1;

    private static final int PAYMENT_PAID = 2;

    private static final int PAYMENT_REFUNDED = 3;

    private static final int PAYMENT_METHOD_WALLET = 1;

    private static final int TRADE_TYPE_OFFLINE = 1;

    private static final int TRADE_TYPE_ONLINE = 2;

    private static final int REFUND_NONE = 0;

    private static final int REFUND_PENDING = 1;

    private static final int REFUND_REJECTED = 3;

    private static final int REFUND_COMPLETED = 4;

    private static final int TRANSACTION_PAYMENT = 3;

    private static final int TRANSACTION_REFUND = 4;

    private static final int TRANSACTION_INCOME = 5;

    private static final int TRANSACTION_FREEZE = 6;

    private static final int TRANSACTION_UNFREEZE = 7;

    private static final int OPERATOR_ROLE_BUYER = 1;

    private static final int OPERATOR_ROLE_SELLER = 2;

    private static final int OPERATOR_ROLE_ADMIN = 3;

    private static final int OPERATOR_ROLE_SYSTEM = 4;

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private static final DateTimeFormatter ORDER_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final DateTimeFormatter TRANSACTION_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final TradeOrderRepository tradeOrderRepository;

    private final TradeDisputeRepository tradeDisputeRepository;

    private final TradeNotificationPublisher notificationPublisher;

    public TradeOrderService(TradeOrderRepository tradeOrderRepository,
                             TradeDisputeRepository tradeDisputeRepository,
                             TradeNotificationPublisher notificationPublisher) {
        this.tradeOrderRepository = tradeOrderRepository;
        this.tradeDisputeRepository = tradeDisputeRepository;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        LoginUserContext user = requireCurrentUser();
        Long serviceId = requirePositiveId(request.serviceId(), "serviceId");
        int quantity = requirePositiveInt(request.quantity(), "quantity");
        int tradeType = normalizeTradeType(request.tradeType());
        Long tradeLocationId = normalizeTradeLocationId(tradeType, request.tradeLocationId());

        TradeServiceSnapshot service = tradeOrderRepository.findServiceForOrder(serviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_EXIST));
        if (!Integer.valueOf(SERVICE_ONLINE).equals(service.status())) {
            throw new BusinessException(ErrorCode.SERVICE_OFFLINE);
        }
        if (user.userId().equals(service.publisherId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "cannot buy your own service");
        }
        if (service.price() == null || service.price().signum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "service price is invalid");
        }

        BigDecimal totalAmount = service.price().multiply(BigDecimal.valueOf(quantity));
        String orderNumber = generateOrderNumber();
        Long orderId = tradeOrderRepository.createOrder(new OrderCreateCommand(
                orderNumber,
                service.serviceId(),
                user.userId(),
                service.publisherId(),
                quantity,
                service.price(),
                totalAmount,
                tradeType,
                tradeLocationId,
                PAYMENT_UNPAID,
                ORDER_UNPAID,
                BigDecimal.ZERO,
                PAYMENT_METHOD_WALLET,
                REFUND_NONE,
                normalizeOptional(request.remark())));

        recordLog(orderId, null, ORDER_UNPAID, user.userId(), OPERATOR_ROLE_BUYER,
                "CREATE", "创建订单");
        publishOrderNotification(service.publisherId(), "收到新订单",
                "你的服务「" + service.title() + "」收到一笔新订单，订单号 " + orderNumber + "，待买家支付。",
                orderId);
        return orderId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, OrderPayRequest request) {
        LoginUserContext user = requireCurrentUser();
        Long normalizedOrderId = requirePositiveId(orderId, "orderId");
        int paymentMethod = normalizePaymentMethod(request == null ? null : request.paymentMethod());
        TradeOrder order = getOrderForUpdate(normalizedOrderId);
        if (!user.userId().equals(order.buyerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the buyer can pay the order");
        }
        if (!Integer.valueOf(ORDER_UNPAID).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order is not unpaid");
        }

        TradeUserWallet buyer = tradeOrderRepository.findUserForUpdate(user.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        BigDecimal amount = order.totalAmount();
        if (buyer.walletBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.ORDER_BALANCE_NOT_ENOUGH);
        }

        BigDecimal balanceAfter = buyer.walletBalance().subtract(amount);
        BigDecimal frozenAfter = buyer.frozenAmount().add(amount);
        if (!tradeOrderRepository.updateUserWallet(buyer.userId(), balanceAfter, frozenAfter)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "wallet update failed");
        }

        recordWalletTransaction(buyer.userId(), TRANSACTION_PAYMENT, amount,
                buyer.walletBalance(), balanceAfter, buyer.frozenAmount(), buyer.frozenAmount(),
                order.id(), "支付订单 " + order.orderNumber());
        recordWalletTransaction(buyer.userId(), TRANSACTION_FREEZE, amount,
                balanceAfter, balanceAfter, buyer.frozenAmount(), frozenAfter,
                order.id(), "冻结订单资金 " + order.orderNumber());

        LocalDateTime now = LocalDateTime.now();
        if (!tradeOrderRepository.markOrderPaid(order.id(), amount, paymentMethod, now)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order payment status changed");
        }
        if (!tradeOrderRepository.incrementServiceOrderCount(order.serviceId(), order.quantity())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "service order count update failed");
        }
        recordLog(order.id(), ORDER_UNPAID, ORDER_PENDING_SHIP, user.userId(), OPERATOR_ROLE_BUYER,
                "PAY", "买家支付，资金冻结，等待卖家发货");
        publishOrderNotification(order.sellerId(), "订单已支付",
                "订单 " + order.orderNumber() + " 已支付，请及时发货。", order.id());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        LoginUserContext user = requireCurrentUser();
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!user.userId().equals(order.buyerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the buyer can cancel the order");
        }
        if (!Integer.valueOf(ORDER_UNPAID).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "only unpaid orders can be cancelled");
        }
        if (!tradeOrderRepository.cancelOrder(order.id())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order status changed");
        }
        recordLog(order.id(), ORDER_UNPAID, ORDER_CANCELLED, user.userId(), OPERATOR_ROLE_BUYER,
                "CANCEL", "买家取消未支付订单");
        publishOrderNotification(order.sellerId(), "订单已取消",
                "订单 " + order.orderNumber() + " 已由买家取消。", order.id());
    }

    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId) {
        LoginUserContext user = requireCurrentUser();
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!user.userId().equals(order.sellerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the seller can ship the order");
        }
        if (!Integer.valueOf(ORDER_PENDING_SHIP).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order is not pending shipment");
        }
        if (Integer.valueOf(REFUND_PENDING).equals(order.refundStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has a pending refund request");
        }
        if (!tradeOrderRepository.shipOrder(order.id(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order status changed");
        }
        recordLog(order.id(), ORDER_PENDING_SHIP, ORDER_PENDING_RECEIPT, user.userId(), OPERATOR_ROLE_SELLER,
                "SELLER_SHIP", "卖家已发货，等待买家确认收货");
        publishOrderNotification(order.buyerId(), "卖家已发货",
                "订单 " + order.orderNumber() + " 已发货，请收到服务后确认完成。", order.id());
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long orderId) {
        LoginUserContext user = requireCurrentUser();
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!user.userId().equals(order.buyerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the buyer can confirm the order");
        }
        if (!Integer.valueOf(ORDER_PENDING_RECEIPT).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order is not pending receipt");
        }
        if (Integer.valueOf(REFUND_PENDING).equals(order.refundStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has a pending refund request");
        }
        ensureNoActiveDispute(order.id());

        settleOrder(order, user.userId(), OPERATOR_ROLE_BUYER,
                "BUYER_CONFIRM", "买家确认收货，交易完成，资金已结算给卖家");
    }

    @Transactional(rollbackFor = Exception.class)
    public void requestRefund(Long orderId, OrderRefundRequest request) {
        LoginUserContext user = requireCurrentUser();
        String reason = requireText(request == null ? null : request.reason(), "reason");
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!user.userId().equals(order.buyerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the buyer can request a refund");
        }
        if (!Integer.valueOf(ORDER_PENDING_SHIP).equals(order.orderStatus())
                && !Integer.valueOf(ORDER_PENDING_RECEIPT).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order status does not support refund");
        }
        if (Integer.valueOf(REFUND_PENDING).equals(order.refundStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "refund request is already pending");
        }
        ensureNoActiveDispute(order.id());
        if (Integer.valueOf(REFUND_COMPLETED).equals(order.refundStatus())
                || Integer.valueOf(PAYMENT_REFUNDED).equals(order.paymentStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has already been refunded");
        }

        if (Integer.valueOf(ORDER_PENDING_SHIP).equals(order.orderStatus())) {
            processRefund(order, reason, user.userId(), OPERATOR_ROLE_BUYER,
                    "REFUND", "买家在发货前申请退款，系统自动退款");
            return;
        }

        if (!tradeOrderRepository.requestRefund(order.id(), reason, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "refund request status changed");
        }
        recordLog(order.id(), order.orderStatus(), order.orderStatus(), user.userId(), OPERATOR_ROLE_BUYER,
                "REFUND_REQUEST", "买家申请退款：" + reason);
        publishOrderNotification(order.sellerId(), "收到退款申请",
                "订单 " + order.orderNumber() + " 收到买家退款申请，请及时处理。", order.id());
    }

    @Transactional(rollbackFor = Exception.class)
    public void sellerHandleRefund(Long orderId, SellerHandleRefundRequest request) {
        LoginUserContext user = requireCurrentUser();
        if (request == null || request.approve() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "approve is required");
        }
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!user.userId().equals(order.sellerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the seller can handle the refund request");
        }
        if (!Integer.valueOf(REFUND_PENDING).equals(order.refundStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "there is no pending refund request");
        }
        ensureNoActiveDispute(order.id());

        if (Boolean.TRUE.equals(request.approve())) {
            String reason = normalizeOptional(order.refundReason());
            processRefund(order, reason == null ? "卖家同意退款" : reason, user.userId(), OPERATOR_ROLE_SELLER,
                    "SELLER_APPROVE_REFUND", "卖家同意退款");
            return;
        }

        String rejectReason = requireText(request.rejectReason(), "rejectReason");
        if (!tradeOrderRepository.rejectRefund(order.id())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "refund status changed");
        }
        recordLog(order.id(), order.orderStatus(), order.orderStatus(), user.userId(), OPERATOR_ROLE_SELLER,
                "SELLER_REJECT_REFUND", "卖家拒绝退款：" + rejectReason);
        publishOrderNotification(order.buyerId(), "退款申请被拒绝",
                "订单 " + order.orderNumber() + " 的退款申请被卖家拒绝，你可以再次沟通或发起纠纷。", order.id());
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminRefundOrder(Long orderId, String reason) {
        LoginUserContext user = requireCurrentUser();
        if (!isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only admin can refund this order");
        }
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!Integer.valueOf(ORDER_PENDING_SHIP).equals(order.orderStatus())
                && !Integer.valueOf(ORDER_PENDING_RECEIPT).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order status does not support refund");
        }
        String normalizedReason = requireText(reason, "reason");
        processRefund(order, normalizedReason, user.userId(), OPERATOR_ROLE_ADMIN,
                "ADMIN_REFUND", "管理员强制退款：" + normalizedReason);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean systemCancelUnpaidOrder(Long orderId, int timeoutHours) {
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!Integer.valueOf(ORDER_UNPAID).equals(order.orderStatus())
                || !Integer.valueOf(PAYMENT_UNPAID).equals(order.paymentStatus())) {
            return false;
        }
        if (!tradeOrderRepository.cancelOrder(order.id())) {
            return false;
        }
        recordLog(order.id(), ORDER_UNPAID, ORDER_CANCELLED, null, OPERATOR_ROLE_SYSTEM,
                "AUTO_CANCEL", String.format("系统自动取消：订单超过%d小时未支付", timeoutHours));
        publishOrderNotification(order.buyerId(), "订单已自动取消",
                String.format("订单 %s 超过%d小时未支付，系统已自动取消。", order.orderNumber(), timeoutHours),
                order.id());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean systemConfirmReceipt(Long orderId, int timeoutDays) {
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!Integer.valueOf(ORDER_PENDING_RECEIPT).equals(order.orderStatus())
                || !Integer.valueOf(PAYMENT_PAID).equals(order.paymentStatus())
                || Integer.valueOf(REFUND_PENDING).equals(order.refundStatus())
                || tradeDisputeRepository.existsActiveByOrderId(order.id())) {
            return false;
        }
        settleOrder(order, null, OPERATOR_ROLE_SYSTEM,
                "AUTO_CONFIRM_RECEIPT", String.format("系统自动确认收货：发货超过%d天", timeoutDays));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean systemRefundTimeoutOrder(Long orderId, int timeoutDays) {
        TradeOrder order = getOrderForUpdate(requirePositiveId(orderId, "orderId"));
        if (!Integer.valueOf(REFUND_PENDING).equals(order.refundStatus())
                || !Integer.valueOf(PAYMENT_PAID).equals(order.paymentStatus())
                || (!Integer.valueOf(ORDER_PENDING_SHIP).equals(order.orderStatus())
                && !Integer.valueOf(ORDER_PENDING_RECEIPT).equals(order.orderStatus()))
                || tradeDisputeRepository.existsActiveByOrderId(order.id())) {
            return false;
        }
        String reason = normalizeOptional(order.refundReason());
        processRefund(order, reason == null ? "卖家超时未处理退款申请" : reason, null, OPERATOR_ROLE_SYSTEM,
                "AUTO_REFUND", String.format("卖家超过%d天未处理退款申请，系统自动退款", timeoutDays));
        return true;
    }

    public OrderListResponse listMyOrders(Integer pageNum, Integer pageSize, Integer orderStatus) {
        LoginUserContext user = requireCurrentUser();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        return OrderListResponse.from(tradeOrderRepository.findOrders(new OrderListQuery(
                user.userId(),
                null,
                normalizeOrderStatusOrNull(orderStatus),
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize)), normalizedPageNum, normalizedPageSize);
    }

    public OrderListResponse listMySellingOrders(Integer pageNum, Integer pageSize, Integer orderStatus) {
        LoginUserContext user = requireCurrentUser();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        return OrderListResponse.from(tradeOrderRepository.findOrders(new OrderListQuery(
                null,
                user.userId(),
                normalizeOrderStatusOrNull(orderStatus),
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize)), normalizedPageNum, normalizedPageSize);
    }

    public OrderDetailResponse getOrderDetail(Long orderId) {
        LoginUserContext user = requireCurrentUser();
        TradeOrder order = tradeOrderRepository.findOrderById(requirePositiveId(orderId, "orderId"))
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXIST));
        if (!isAdmin(user) && !user.userId().equals(order.buyerId()) && !user.userId().equals(order.sellerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to view this order");
        }
        return OrderDetailResponse.from(order);
    }

    private TradeOrder getOrderForUpdate(Long orderId) {
        return tradeOrderRepository.findOrderForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXIST));
    }

    private void ensureNoActiveDispute(Long orderId) {
        if (tradeDisputeRepository.existsActiveByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has a pending dispute");
        }
    }

    private TradeUserWallet lockUserFirst(Long buyerId, Long sellerId) {
        Long firstId = buyerId < sellerId ? buyerId : sellerId;
        return tradeOrderRepository.findUserForUpdate(firstId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
    }

    private TradeUserWallet lockUserSecond(Long buyerId, Long sellerId) {
        Long secondId = buyerId < sellerId ? sellerId : buyerId;
        return tradeOrderRepository.findUserForUpdate(secondId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
    }

    private void processRefund(TradeOrder order, String reason, Long operatorId,
                               Integer operatorRole, String actionType, String logMessage) {
        BigDecimal refundAmount = order.frozenAmount();
        if (refundAmount == null || refundAmount.signum() <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has no refundable frozen amount");
        }

        TradeUserWallet buyer = tradeOrderRepository.findUserForUpdate(order.buyerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        BigDecimal balanceBefore = zeroIfNull(buyer.walletBalance());
        BigDecimal frozenBefore = zeroIfNull(buyer.frozenAmount());
        if (frozenBefore.compareTo(refundAmount) < 0) {
            throw new BusinessException(ErrorCode.WALLET_FROZEN_NOT_ENOUGH);
        }

        BigDecimal frozenAfter = frozenBefore.subtract(refundAmount);
        BigDecimal balanceAfter = balanceBefore.add(refundAmount);
        if (!tradeOrderRepository.updateUserWallet(buyer.userId(), balanceAfter, frozenAfter)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "buyer wallet update failed");
        }

        recordWalletTransaction(buyer.userId(), TRANSACTION_UNFREEZE, refundAmount,
                balanceBefore, balanceBefore, frozenBefore, frozenAfter,
                order.id(), "退款解冻订单资金 " + order.orderNumber());
        recordWalletTransaction(buyer.userId(), TRANSACTION_REFUND, refundAmount,
                balanceBefore, balanceAfter, frozenAfter, frozenAfter,
                order.id(), "订单退款 " + order.orderNumber());

        if (!tradeOrderRepository.markOrderRefunded(order.id(), reason, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order refund status changed");
        }
        recordLog(order.id(), order.orderStatus(), ORDER_FAILED, operatorId, operatorRole,
                actionType, logMessage);
        publishOrderNotification(order.buyerId(), "退款已完成",
                "订单 " + order.orderNumber() + " 已退款，退款原因：" + reason, order.id());
    }

    private void settleOrder(TradeOrder order, Long operatorId,
                             Integer operatorRole, String actionType, String logMessage) {
        BigDecimal amount = order.frozenAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ErrorCode.WALLET_FROZEN_NOT_ENOUGH);
        }

        TradeUserWallet first = lockUserFirst(order.buyerId(), order.sellerId());
        TradeUserWallet second = lockUserSecond(order.buyerId(), order.sellerId());
        TradeUserWallet buyer = order.buyerId().equals(first.userId()) ? first : second;
        TradeUserWallet seller = order.sellerId().equals(first.userId()) ? first : second;
        if (buyer.frozenAmount().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.WALLET_FROZEN_NOT_ENOUGH);
        }

        BigDecimal buyerFrozenAfter = buyer.frozenAmount().subtract(amount);
        BigDecimal sellerBalanceAfter = seller.walletBalance().add(amount);
        if (!tradeOrderRepository.updateUserWallet(buyer.userId(), buyer.walletBalance(), buyerFrozenAfter)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "buyer wallet update failed");
        }
        if (!tradeOrderRepository.updateUserWallet(seller.userId(), sellerBalanceAfter, seller.frozenAmount())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "seller wallet update failed");
        }

        recordWalletTransaction(buyer.userId(), TRANSACTION_UNFREEZE, amount,
                buyer.walletBalance(), buyer.walletBalance(), buyer.frozenAmount(), buyerFrozenAfter,
                order.id(), "确认收货解冻资金 " + order.orderNumber());
        recordWalletTransaction(seller.userId(), TRANSACTION_INCOME, amount,
                seller.walletBalance(), sellerBalanceAfter, seller.frozenAmount(), seller.frozenAmount(),
                order.id(), "订单收入 " + order.orderNumber());

        if (!tradeOrderRepository.completeOrder(order.id(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order status changed");
        }
        recordLog(order.id(), order.orderStatus(), ORDER_COMPLETED, operatorId, operatorRole,
                actionType, logMessage);
        publishOrderNotification(order.sellerId(), "订单已完成",
                "订单 " + order.orderNumber() + " 已完成，订单收入已结算。", order.id());
        if (Integer.valueOf(OPERATOR_ROLE_SYSTEM).equals(operatorRole)) {
            publishOrderNotification(order.buyerId(), "订单已自动完成",
                    "订单 " + order.orderNumber() + " 已由系统自动确认完成。", order.id());
        }
    }

    private void recordLog(Long orderId, Integer oldStatus, Integer newStatus, Long operatorId,
                           Integer operatorRole, String actionType, String remark) {
        tradeOrderRepository.insertOrderLog(new OrderLogCommand(
                orderId,
                oldStatus,
                newStatus,
                operatorId,
                operatorRole,
                actionType,
                remark));
    }

    private void recordWalletTransaction(Long userId, Integer transactionType, BigDecimal amount,
                                         BigDecimal balanceBefore, BigDecimal balanceAfter,
                                         BigDecimal frozenBefore, BigDecimal frozenAfter,
                                         Long relatedOrderId, String remark) {
        tradeOrderRepository.insertWalletTransaction(new WalletTransactionCommand(
                userId,
                transactionType,
                amount,
                balanceBefore,
                balanceAfter,
                frozenBefore,
                frozenAfter,
                relatedOrderId,
                generateTransactionNumber(userId),
                remark));
    }

    private void publishOrderNotification(Long recipientId, String title, String content, Long orderId) {
        notificationPublisher.publishOrderNotification(recipientId, title, content, orderId);
    }

    private LoginUserContext requireCurrentUser() {
        return UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
    }

    private boolean isAdmin(LoginUserContext user) {
        return Integer.valueOf(ADMIN_ROLE).equals(user.role());
    }

    private Long requirePositiveId(Long value, String name) {
        if (value == null || value <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must be positive");
        }
        return value;
    }

    private int requirePositiveInt(Integer value, String name) {
        if (value == null || value <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must be positive");
        }
        return value;
    }

    private int normalizeTradeType(Integer tradeType) {
        if (tradeType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "tradeType is required");
        }
        if (tradeType == TRADE_TYPE_OFFLINE || tradeType == TRADE_TYPE_ONLINE) {
            return tradeType;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "tradeType is invalid");
    }

    private Long normalizeTradeLocationId(int tradeType, Long tradeLocationId) {
        if (tradeType == TRADE_TYPE_OFFLINE) {
            return requirePositiveId(tradeLocationId, "tradeLocationId");
        }
        if (tradeLocationId == null) {
            return null;
        }
        return requirePositiveId(tradeLocationId, "tradeLocationId");
    }

    private int normalizePaymentMethod(Integer paymentMethod) {
        if (paymentMethod == null) {
            return PAYMENT_METHOD_WALLET;
        }
        if (paymentMethod == PAYMENT_METHOD_WALLET) {
            return paymentMethod;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "paymentMethod is invalid");
    }

    private Integer normalizeOrderStatusOrNull(Integer orderStatus) {
        if (orderStatus == null) {
            return null;
        }
        if (orderStatus >= ORDER_UNPAID && orderStatus <= ORDER_FAILED) {
            return orderStatus;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "orderStatus is invalid");
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String requireText(String value, String name) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must not be blank");
        }
        return normalized;
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null) {
            return DEFAULT_PAGE_NUM;
        }
        if (pageNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageNum must be positive");
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String generateOrderNumber() {
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "ORDER" + LocalDateTime.now().format(ORDER_NUMBER_FORMAT) + random;
    }

    private String generateTransactionNumber(Long userId) {
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "WT" + LocalDateTime.now().format(TRANSACTION_NUMBER_FORMAT) + userId + random;
    }
}
