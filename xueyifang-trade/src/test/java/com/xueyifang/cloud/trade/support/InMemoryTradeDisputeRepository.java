package com.xueyifang.cloud.trade.support;

import com.xueyifang.cloud.trade.repository.DisputeCreateCommand;
import com.xueyifang.cloud.trade.repository.DisputeHandleCommand;
import com.xueyifang.cloud.trade.repository.DisputeListQuery;
import com.xueyifang.cloud.trade.repository.DisputePage;
import com.xueyifang.cloud.trade.repository.TradeDispute;
import com.xueyifang.cloud.trade.repository.TradeDisputeRepository;
import com.xueyifang.cloud.trade.repository.TradeOrder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTradeDisputeRepository implements TradeDisputeRepository {

    private final InMemoryTradeOrderRepository orderRepository;

    private final Map<Long, StoredDispute> disputes = new HashMap<>();

    private long nextDisputeId = 1L;

    public InMemoryTradeDisputeRepository(InMemoryTradeOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public TradeDispute getDispute(Long disputeId) {
        return findById(disputeId).orElse(null);
    }

    @Override
    public Long createDispute(DisputeCreateCommand command) {
        Long disputeId = nextDisputeId++;
        LocalDateTime now = LocalDateTime.parse("2026-06-14T03:00:00").plusSeconds(disputeId);
        disputes.put(disputeId, new StoredDispute(
                disputeId,
                command.orderId(),
                command.complainantId(),
                command.respondentId(),
                1,
                command.reason(),
                command.evidence(),
                null,
                null,
                null,
                null,
                now,
                now));
        return disputeId;
    }

    @Override
    public Optional<TradeDispute> findById(Long disputeId) {
        return Optional.ofNullable(disputes.get(disputeId)).map(this::mapDispute);
    }

    @Override
    public Optional<TradeDispute> findByIdForUpdate(Long disputeId) {
        return findById(disputeId);
    }

    @Override
    public DisputePage findDisputes(DisputeListQuery query) {
        List<TradeDispute> matched = disputes.values().stream()
                .filter(dispute -> query.admin()
                        || query.userId().equals(dispute.complainantId())
                        || query.userId().equals(dispute.respondentId()))
                .filter(dispute -> query.status() == null || query.status().equals(dispute.status()))
                .sorted(Comparator.comparing(StoredDispute::createTime).reversed())
                .map(this::mapDispute)
                .toList();
        List<TradeDispute> records = matched.stream()
                .skip(query.offset())
                .limit(query.limit())
                .toList();
        return new DisputePage(records, matched.size());
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return disputes.values().stream()
                .anyMatch(dispute -> orderId.equals(dispute.orderId()));
    }

    @Override
    public boolean existsActiveByOrderId(Long orderId) {
        return disputes.values().stream()
                .anyMatch(dispute -> orderId.equals(dispute.orderId()) && Integer.valueOf(1).equals(dispute.status()));
    }

    @Override
    public boolean handleDispute(DisputeHandleCommand command) {
        StoredDispute dispute = disputes.get(command.disputeId());
        if (dispute == null || !Integer.valueOf(1).equals(dispute.status())) {
            return false;
        }
        disputes.put(command.disputeId(), new StoredDispute(
                dispute.id(),
                dispute.orderId(),
                dispute.complainantId(),
                dispute.respondentId(),
                command.status(),
                dispute.reason(),
                dispute.evidence(),
                command.handleResult(),
                command.handleRemark(),
                command.handlerId(),
                command.handleTime(),
                dispute.createTime(),
                command.handleTime()));
        return true;
    }

    private TradeDispute mapDispute(StoredDispute dispute) {
        TradeOrder order = orderRepository.getOrder(dispute.orderId());
        return new TradeDispute(
                dispute.id(),
                dispute.orderId(),
                dispute.complainantId(),
                dispute.respondentId(),
                dispute.status(),
                dispute.reason(),
                dispute.evidence(),
                dispute.handleResult(),
                dispute.handleRemark(),
                dispute.handlerId(),
                dispute.handleTime(),
                dispute.createTime(),
                dispute.updateTime(),
                order == null ? null : order.orderNumber(),
                order == null ? null : order.serviceId(),
                order == null ? null : order.serviceTitle(),
                order == null ? null : order.serviceImage(),
                order == null ? null : order.totalAmount(),
                order == null ? null : order.orderStatus(),
                order == null ? null : order.paymentStatus(),
                order == null ? null : order.refundStatus(),
                order == null ? null : order.buyerId(),
                order == null ? null : order.buyerName(),
                order == null ? null : order.buyerAvatar(),
                order == null ? null : order.sellerId(),
                order == null ? null : order.sellerName(),
                order == null ? null : order.sellerAvatar());
    }

    private record StoredDispute(
            Long id,
            Long orderId,
            Long complainantId,
            Long respondentId,
            Integer status,
            String reason,
            String evidence,
            String handleResult,
            String handleRemark,
            Long handlerId,
            LocalDateTime handleTime,
            LocalDateTime createTime,
            LocalDateTime updateTime) {
    }
}
