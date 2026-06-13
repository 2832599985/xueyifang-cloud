package com.xueyifang.cloud.service.repository;

import java.util.List;

public record ServicePage(
        List<ServiceItem> records,
        long total) {
}
