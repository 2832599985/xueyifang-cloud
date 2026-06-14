package com.xueyifang.cloud.trade.service;

import com.xueyifang.cloud.trade.config.TradeOrderTaskProperties;
import com.xueyifang.cloud.trade.repository.TradeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
public class TradeOrderTaskService {

    private static final Logger log = LoggerFactory.getLogger(TradeOrderTaskService.class);

    private final TradeOrderRepository tradeOrderRepository;

    private final TradeOrderService tradeOrderService;

    private final TradeOrderTaskProperties properties;

    private final Clock clock;

    public TradeOrderTaskService(TradeOrderRepository tradeOrderRepository,
                                 TradeOrderService tradeOrderService,
                                 TradeOrderTaskProperties properties,
                                 Clock clock) {
        this.tradeOrderRepository = tradeOrderRepository;
        this.tradeOrderService = tradeOrderService;
        this.properties = properties;
        this.clock = clock;
    }

    public int autoCancelUnpaidOrders() {
        int timeoutHours = properties.normalizedUnpaidTimeoutHours();
        LocalDateTime deadline = LocalDateTime.now(clock).minusHours(timeoutHours);
        List<Long> orderIds = tradeOrderRepository.findUnpaidOrderIdsCreatedAtOrBefore(
                deadline,
                properties.normalizedBatchSize());
        return processOrders("auto-cancel", orderIds,
                orderId -> tradeOrderService.systemCancelUnpaidOrder(orderId, timeoutHours));
    }

    public int autoConfirmReceipt() {
        int timeoutDays = properties.normalizedAutoConfirmReceiptDays();
        LocalDateTime deadline = LocalDateTime.now(clock).minusDays(timeoutDays);
        List<Long> orderIds = tradeOrderRepository.findPendingReceiptOrderIdsShippedAtOrBefore(
                deadline,
                properties.normalizedBatchSize());
        return processOrders("auto-confirm-receipt", orderIds,
                orderId -> tradeOrderService.systemConfirmReceipt(orderId, timeoutDays));
    }

    public int autoRefundTimeout() {
        int timeoutDays = properties.normalizedSellerRefundTimeoutDays();
        LocalDateTime deadline = LocalDateTime.now(clock).minusDays(timeoutDays);
        List<Long> orderIds = tradeOrderRepository.findPendingRefundOrderIdsRequestedAtOrBefore(
                deadline,
                properties.normalizedBatchSize());
        return processOrders("auto-refund", orderIds,
                orderId -> tradeOrderService.systemRefundTimeoutOrder(orderId, timeoutDays));
    }

    private int processOrders(String taskName, List<Long> orderIds, Function<Long, Boolean> processor) {
        int processed = 0;
        for (Long orderId : orderIds) {
            try {
                if (Boolean.TRUE.equals(processor.apply(orderId))) {
                    processed++;
                }
            } catch (RuntimeException exception) {
                log.warn("Trade order task {} skipped order {}", taskName, orderId, exception);
            }
        }
        return processed;
    }
}
