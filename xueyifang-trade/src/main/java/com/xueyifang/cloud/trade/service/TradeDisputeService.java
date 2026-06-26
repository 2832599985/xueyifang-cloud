package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.trade.dto.DisputeCreateRequest;
import com.xueyifang.cloud.trade.dto.DisputeHandleRequest;
import com.xueyifang.cloud.trade.dto.DisputeListResponse;
import com.xueyifang.cloud.trade.dto.DisputeResponse;
import com.xueyifang.cloud.trade.notification.TradeNotificationPublisher;
import com.xueyifang.cloud.trade.repository.DisputeCreateCommand;
import com.xueyifang.cloud.trade.repository.DisputeHandleCommand;
import com.xueyifang.cloud.trade.repository.DisputeListQuery;
import com.xueyifang.cloud.trade.repository.OrderLogCommand;
import com.xueyifang.cloud.trade.repository.TradeDispute;
import com.xueyifang.cloud.trade.repository.TradeDisputeRepository;
import com.xueyifang.cloud.trade.repository.TradeOrder;
import com.xueyifang.cloud.trade.repository.TradeOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TradeDisputeService {

    private static final int ORDER_PENDING_RECEIPT = 3;

    private static final int ORDER_FAILED = 6;

    private static final int PAYMENT_PAID = 2;

    private static final int REFUND_REJECTED = 3;

    private static final int DISPUTE_PENDING = 1;

    private static final int DISPUTE_REFUNDED = 2;

    private static final int DISPUTE_REJECTED = 3;

    private static final int OPERATOR_ROLE_BUYER = 1;

    private static final int OPERATOR_ROLE_ADMIN = 3;

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final TradeOrderRepository tradeOrderRepository;

    private final TradeDisputeRepository tradeDisputeRepository;

    private final TradeOrderService tradeOrderService;

    private final TradeNotificationPublisher notificationPublisher;

    public TradeDisputeService(TradeOrderRepository tradeOrderRepository,
                               TradeDisputeRepository tradeDisputeRepository,
                               TradeOrderService tradeOrderService,
                               TradeNotificationPublisher notificationPublisher) {
        this.tradeOrderRepository = tradeOrderRepository;
        this.tradeDisputeRepository = tradeDisputeRepository;
        this.tradeOrderService = tradeOrderService;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createDispute(DisputeCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        LoginUserContext user = requireCurrentUser();
        Long orderId = requirePositiveId(request.orderId(), "orderId");
        String reason = requireText(request.effectiveReason(), "reason");

        TradeOrder order = tradeOrderRepository.findOrderForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXIST));
        if (!user.userId().equals(order.buyerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the buyer can create a dispute");
        }
        if (!Integer.valueOf(ORDER_PENDING_RECEIPT).equals(order.orderStatus())
                || !Integer.valueOf(PAYMENT_PAID).equals(order.paymentStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "order status does not support dispute");
        }
        if (!Integer.valueOf(REFUND_REJECTED).equals(order.refundStatus())) {
            throw new BusinessException(ErrorCode.DISPUTE_STATUS_ERROR,
                    "refund must be rejected before creating a dispute");
        }
        if (tradeDisputeRepository.existsByOrderId(order.id())) {
            throw new BusinessException(ErrorCode.DISPUTE_STATUS_ERROR, "order already has a dispute");
        }

        Long disputeId = tradeDisputeRepository.createDispute(new DisputeCreateCommand(
                order.id(),
                user.userId(),
                order.sellerId(),
                reason,
                normalizeOptional(request.evidence()),
                request.disputeType()));
        recordLog(order.id(), order.orderStatus(), order.orderStatus(), user.userId(), OPERATOR_ROLE_BUYER,
                "DISPUTE_CREATE", "买家发起纠纷：" + reason);
        publishDisputeNotification(order.sellerId(), "收到订单纠纷",
                "订单 " + order.orderNumber() + " 收到买家发起的纠纷，请等待管理员处理。", disputeId);
        return disputeId;
    }

    public DisputeListResponse listMyDisputes(Integer pageNum, Integer pageSize, Integer status) {
        LoginUserContext user = requireCurrentUser();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        return DisputeListResponse.from(tradeDisputeRepository.findDisputes(new DisputeListQuery(
                user.userId(),
                normalizeStatusOrNull(status),
                false,
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize)), normalizedPageNum, normalizedPageSize);
    }

    public DisputeListResponse listAdminDisputes(Integer pageNum, Integer pageSize, Integer status) {
        LoginUserContext user = requireCurrentUser();
        if (!isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only admin can list disputes");
        }
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        return DisputeListResponse.from(tradeDisputeRepository.findDisputes(new DisputeListQuery(
                null,
                normalizeStatusOrNull(status),
                true,
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize)), normalizedPageNum, normalizedPageSize);
    }

    public DisputeResponse getDisputeDetail(Long disputeId) {
        LoginUserContext user = requireCurrentUser();
        TradeDispute dispute = tradeDisputeRepository.findById(requirePositiveId(disputeId, "disputeId"))
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPUTE_NOT_EXIST));
        if (!isAdmin(user)
                && !user.userId().equals(dispute.complainantId())
                && !user.userId().equals(dispute.respondentId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to view this dispute");
        }
        return DisputeResponse.from(dispute);
    }

    public DisputeResponse getDisputeDetailByOrderIdForAdmin(Long orderId) {
        LoginUserContext user = requireCurrentUser();
        if (!isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only admin can view disputes by order");
        }
        return tradeDisputeRepository.findByOrderId(requirePositiveId(orderId, "orderId"))
                .map(DisputeResponse::from)
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleDispute(Long disputeId, DisputeHandleRequest request) {
        if (request == null || request.effectiveApproveRefund() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "approveRefund is required");
        }
        Boolean approveRefund = request.effectiveApproveRefund();
        String effectiveHandleRemark = request.effectiveHandleRemark();
        LoginUserContext user = requireCurrentUser();
        if (!isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only admin can handle disputes");
        }
        TradeDispute dispute = tradeDisputeRepository.findByIdForUpdate(requirePositiveId(disputeId, "disputeId"))
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPUTE_NOT_EXIST));
        if (!Integer.valueOf(DISPUTE_PENDING).equals(dispute.status())) {
            throw new BusinessException(ErrorCode.DISPUTE_STATUS_ERROR, "dispute has already been handled");
        }

        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(approveRefund)) {
            String remark = firstNonBlank(normalizeOptional(effectiveHandleRemark), "管理员支持买家，已退款");
            tradeOrderService.adminRefundOrder(dispute.orderId(), "纠纷处理退款：" + remark);
            if (!tradeDisputeRepository.handleDispute(new DisputeHandleCommand(
                    dispute.id(), DISPUTE_REFUNDED, "REFUND_APPROVED", remark, user.userId(), now))) {
                throw new BusinessException(ErrorCode.DISPUTE_STATUS_ERROR, "dispute status changed");
            }
            recordLog(dispute.orderId(), dispute.orderStatus(), ORDER_FAILED, user.userId(), OPERATOR_ROLE_ADMIN,
                    "DISPUTE_REFUND", "管理员处理纠纷并退款：" + remark);
            publishDisputeNotification(dispute.complainantId(), "纠纷处理完成",
                    "订单 " + dispute.orderNumber() + " 的纠纷已处理，管理员支持退款。", dispute.id());
            publishDisputeNotification(dispute.respondentId(), "纠纷处理完成",
                    "订单 " + dispute.orderNumber() + " 的纠纷已处理，管理员支持买家退款。", dispute.id());
            return;
        }

        String remark = requireText(effectiveHandleRemark, "handleRemark");
        if (!tradeDisputeRepository.handleDispute(new DisputeHandleCommand(
                dispute.id(), DISPUTE_REJECTED, "REJECTED", remark, user.userId(), now))) {
            throw new BusinessException(ErrorCode.DISPUTE_STATUS_ERROR, "dispute status changed");
        }
        recordLog(dispute.orderId(), dispute.orderStatus(), dispute.orderStatus(), user.userId(), OPERATOR_ROLE_ADMIN,
                "DISPUTE_REJECT", "管理员驳回纠纷：" + remark);
        publishDisputeNotification(dispute.complainantId(), "纠纷处理完成",
                "订单 " + dispute.orderNumber() + " 的纠纷已被管理员驳回。", dispute.id());
        publishDisputeNotification(dispute.respondentId(), "纠纷处理完成",
                "订单 " + dispute.orderNumber() + " 的纠纷已被管理员驳回。", dispute.id());
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

    private void publishDisputeNotification(Long recipientId, String title, String content, Long disputeId) {
        notificationPublisher.publishDisputeNotification(recipientId, title, content, disputeId);
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

    private Integer normalizeStatusOrNull(Integer status) {
        if (status == null) {
            return null;
        }
        if (status >= DISPUTE_PENDING && status <= DISPUTE_REJECTED) {
            return status;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "status is invalid");
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

    private String requireText(String value, String name) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must not be blank");
        }
        return normalized;
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
