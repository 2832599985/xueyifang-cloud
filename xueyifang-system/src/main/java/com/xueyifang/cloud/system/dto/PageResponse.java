package com.xueyifang.cloud.system.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> records,
        long total,
        int current,
        int pageNum,
        int pageSize,
        int pages) {

    public static <T> PageResponse<T> of(List<T> records, long total, int pageNum, int pageSize) {
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        return new PageResponse<>(records, total, pageNum, pageNum, pageSize, pages);
    }
}
