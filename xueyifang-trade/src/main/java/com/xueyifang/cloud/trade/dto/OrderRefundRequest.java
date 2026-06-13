package com.xueyifang.cloud.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderRefundRequest(
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 500) String remark) {
}
