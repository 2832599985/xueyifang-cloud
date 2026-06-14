package com.xueyifang.cloud.trade.repository;

import java.util.Optional;

public interface TradeDisputeRepository {

    Long createDispute(DisputeCreateCommand command);

    Optional<TradeDispute> findById(Long disputeId);

    Optional<TradeDispute> findByIdForUpdate(Long disputeId);

    Optional<TradeDispute> findByOrderId(Long orderId);

    DisputePage findDisputes(DisputeListQuery query);

    boolean existsByOrderId(Long orderId);

    boolean existsActiveByOrderId(Long orderId);

    boolean handleDispute(DisputeHandleCommand command);
}
