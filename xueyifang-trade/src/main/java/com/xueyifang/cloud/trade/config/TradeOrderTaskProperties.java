package com.xueyifang.cloud.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xueyifang.trade.tasks")
public class TradeOrderTaskProperties {

    private boolean enabled = true;

    private int batchSize = 100;

    private int unpaidTimeoutHours = 24;

    private int autoConfirmReceiptDays = 7;

    private int sellerRefundTimeoutDays = 3;

    private String autoCancelCron = "0 */10 * * * ?";

    private String autoConfirmCron = "0 0 * * * ?";

    private String autoRefundCron = "0 30 * * * ?";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getUnpaidTimeoutHours() {
        return unpaidTimeoutHours;
    }

    public void setUnpaidTimeoutHours(int unpaidTimeoutHours) {
        this.unpaidTimeoutHours = unpaidTimeoutHours;
    }

    public int getAutoConfirmReceiptDays() {
        return autoConfirmReceiptDays;
    }

    public void setAutoConfirmReceiptDays(int autoConfirmReceiptDays) {
        this.autoConfirmReceiptDays = autoConfirmReceiptDays;
    }

    public int getSellerRefundTimeoutDays() {
        return sellerRefundTimeoutDays;
    }

    public void setSellerRefundTimeoutDays(int sellerRefundTimeoutDays) {
        this.sellerRefundTimeoutDays = sellerRefundTimeoutDays;
    }

    public String getAutoCancelCron() {
        return autoCancelCron;
    }

    public void setAutoCancelCron(String autoCancelCron) {
        this.autoCancelCron = autoCancelCron;
    }

    public String getAutoConfirmCron() {
        return autoConfirmCron;
    }

    public void setAutoConfirmCron(String autoConfirmCron) {
        this.autoConfirmCron = autoConfirmCron;
    }

    public String getAutoRefundCron() {
        return autoRefundCron;
    }

    public void setAutoRefundCron(String autoRefundCron) {
        this.autoRefundCron = autoRefundCron;
    }

    public int normalizedBatchSize() {
        return Math.max(1, batchSize);
    }

    public int normalizedUnpaidTimeoutHours() {
        return Math.max(1, unpaidTimeoutHours);
    }

    public int normalizedAutoConfirmReceiptDays() {
        return Math.max(1, autoConfirmReceiptDays);
    }

    public int normalizedSellerRefundTimeoutDays() {
        return Math.max(1, sellerRefundTimeoutDays);
    }
}
