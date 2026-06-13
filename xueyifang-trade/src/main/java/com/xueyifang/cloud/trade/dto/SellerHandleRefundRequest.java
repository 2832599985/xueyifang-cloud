package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SellerHandleRefundRequest(
        @NotNull Boolean approve,
        @Size(max = 500) String rejectReason) {
}
