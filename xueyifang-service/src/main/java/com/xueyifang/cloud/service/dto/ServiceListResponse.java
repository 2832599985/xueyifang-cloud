package com.xueyifang.cloud.service.dto;

import java.util.List;

public record ServiceListResponse(
        List<ServiceSummaryResponse> records,
        long total,
        int pageNum,
        int current,
        int pageSize,
        int pages) {
}
