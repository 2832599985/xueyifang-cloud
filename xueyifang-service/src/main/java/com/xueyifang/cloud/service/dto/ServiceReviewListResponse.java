package com.xueyifang.cloud.service.dto;

import java.util.List;

public record ServiceReviewListResponse(
        List<ServiceReviewResponse> records,
        long total,
        int size,
        int current,
        int pages,
        int pageNum,
        int pageSize) {
}
