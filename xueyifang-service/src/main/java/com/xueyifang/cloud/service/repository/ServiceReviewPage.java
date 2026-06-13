package com.xueyifang.cloud.service.repository;

import java.util.List;

public record ServiceReviewPage(
        List<ServiceReviewItem> records,
        long total) {
}
