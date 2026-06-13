package com.xueyifang.cloud.service.repository;

public interface ServiceInteractionRepository {

    boolean addFavorite(Long userId, Long serviceId);

    boolean removeFavorite(Long userId, Long serviceId);

    boolean existsFavorite(Long userId, Long serviceId);

    FavoritePage findFavoritesByUser(Long userId, int offset, int limit);

    ServiceReviewPage findReviewsByService(Long serviceId, int offset, int limit);

    boolean existsReviewByOrderId(Long orderId);
}
