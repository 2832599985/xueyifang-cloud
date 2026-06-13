package com.xueyifang.cloud.service.dto;

import com.xueyifang.cloud.service.repository.FavoriteItem;

import java.time.LocalDateTime;

public record FavoriteListItemResponse(
        Long favoriteId,
        ServiceSummaryResponse service,
        String sellerName,
        LocalDateTime createTime) {

    public static FavoriteListItemResponse from(FavoriteItem item) {
        return new FavoriteListItemResponse(
                item.favoriteId(),
                ServiceSummaryResponse.from(item.service()),
                item.sellerName(),
                item.createTime());
    }
}
