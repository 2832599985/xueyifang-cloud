package com.xueyifang.cloud.service.dto;

import java.util.List;

public record FavoriteListResponse(
        List<FavoriteListItemResponse> records,
        long total,
        int size,
        int current,
        int pages,
        int pageNum,
        int pageSize) {
}
