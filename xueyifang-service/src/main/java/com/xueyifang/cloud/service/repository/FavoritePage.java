package com.xueyifang.cloud.service.repository;

import java.util.List;

public record FavoritePage(
        List<FavoriteItem> records,
        long total) {
}
