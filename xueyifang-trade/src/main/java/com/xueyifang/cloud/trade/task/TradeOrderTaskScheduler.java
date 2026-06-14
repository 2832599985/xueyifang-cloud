package com.xueyifang.cloud.trade.task;

import com.xueyifang.cloud.trade.config.TradeOrderTaskProperties;
import com.xueyifang.cloud.trade.service.TradeOrderTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TradeOrderTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(TradeOrderTaskScheduler.class);

    private final TradeOrderTaskService tradeOrderTaskService;

    private final TradeOrderTaskProperties properties;

    public TradeOrderTaskScheduler(TradeOrderTaskService tradeOrderTaskService,
                                   TradeOrderTaskProperties properties) {
        this.tradeOrderTaskService = tradeOrderTaskService;
        this.properties = properties;
    }

    @Scheduled(cron = "${xueyifang.trade.tasks.auto-cancel-cron:0 */10 * * * ?}")
    public void autoCancelUnpaidOrders() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            int processed = tradeOrderTaskService.autoCancelUnpaidOrders();
            log.info("Trade order auto-cancel task completed, processed={}", processed);
        } catch (RuntimeException exception) {
            log.error("Trade order auto-cancel task failed", exception);
        }
    }

    @Scheduled(cron = "${xueyifang.trade.tasks.auto-confirm-cron:0 0 * * * ?}")
    public void autoConfirmReceipt() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            int processed = tradeOrderTaskService.autoConfirmReceipt();
            log.info("Trade order auto-confirm-receipt task completed, processed={}", processed);
        } catch (RuntimeException exception) {
            log.error("Trade order auto-confirm-receipt task failed", exception);
        }
    }

    @Scheduled(cron = "${xueyifang.trade.tasks.auto-refund-cron:0 30 * * * ?}")
    public void autoRefundTimeout() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            int processed = tradeOrderTaskService.autoRefundTimeout();
            log.info("Trade order auto-refund task completed, processed={}", processed);
        } catch (RuntimeException exception) {
            log.error("Trade order auto-refund task failed", exception);
        }
    }
}
