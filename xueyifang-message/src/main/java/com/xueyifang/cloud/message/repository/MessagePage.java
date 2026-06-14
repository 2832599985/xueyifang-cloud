package com.xueyifang.cloud.message.repository;

import java.util.List;

public record MessagePage<T>(
        List<T> records,
        long total) {
}
