package com.xueyifang.cloud.service.repository;

import java.time.LocalDateTime;

public record FavoriteItem(
        Long favoriteId,
        ServiceItem service,
        String sellerName,
        LocalDateTime createTime) {
}
