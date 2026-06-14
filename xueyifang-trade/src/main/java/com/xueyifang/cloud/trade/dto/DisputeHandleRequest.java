package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisputeHandleRequest(
        @NotNull Boolean approveRefund,
        @Size(max = 500) String handleRemark) {
}
