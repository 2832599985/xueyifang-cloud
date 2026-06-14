package com.xueyifang.cloud.trade.repository;

import java.time.LocalDateTime;

public record DisputeHandleCommand(
        Long disputeId,
        Integer status,
        String handleResult,
        String handleRemark,
        Long handlerId,
        LocalDateTime handleTime) {
}
