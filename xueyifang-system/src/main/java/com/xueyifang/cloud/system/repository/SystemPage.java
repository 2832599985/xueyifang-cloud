package com.xueyifang.cloud.system.repository;

import java.util.List;

public record SystemPage<T>(
        List<T> records,
        long total) {
}
