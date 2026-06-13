package com.xueyifang.cloud.service.support;

import com.xueyifang.cloud.service.repository.FavoriteItem;
import com.xueyifang.cloud.service.repository.FavoritePage;
import com.xueyifang.cloud.service.repository.ServiceInteractionRepository;
import com.xueyifang.cloud.service.repository.ServiceReviewItem;
import com.xueyifang.cloud.service.repository.ServiceReviewPage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryServiceInteractionRepository implements ServiceInteractionRepository {

    private final InMemoryServiceCatalogRepository serviceCatalogRepository;

    private final Map<String, FavoriteEntry> favorites = new LinkedHashMap<>();

    private final List<ServiceReviewItem> reviews = new ArrayList<>();

    private long nextFavoriteId = 1L;

    public InMemoryServiceInteractionRepository(InMemoryServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public void putFavorite(Long favoriteId, Long userId, Long serviceId, String sellerName, LocalDateTime createTime) {
        favorites.put(favoriteKey(userId, serviceId),
                new FavoriteEntry(favoriteId, userId, serviceId, sellerName, createTime));
        nextFavoriteId = Math.max(nextFavoriteId, favoriteId + 1);
    }

    public void putReview(ServiceReviewItem review) {
        reviews.add(review);
    }

    public void clearInteractions() {
        favorites.clear();
        reviews.clear();
        nextFavoriteId = 1L;
    }

    @Override
    public boolean addFavorite(Long userId, Long serviceId) {
        String key = favoriteKey(userId, serviceId);
        if (favorites.containsKey(key)) {
            return false;
        }

        favorites.put(key, new FavoriteEntry(
                nextFavoriteId++,
                userId,
                serviceId,
                null,
                LocalDateTime.parse("2026-06-14T00:00:00")));
        return true;
    }

    @Override
    public boolean removeFavorite(Long userId, Long serviceId) {
        return favorites.remove(favoriteKey(userId, serviceId)) != null;
    }

    @Override
    public boolean existsFavorite(Long userId, Long serviceId) {
        return favorites.containsKey(favoriteKey(userId, serviceId));
    }

    @Override
    public FavoritePage findFavoritesByUser(Long userId, int offset, int limit) {
        List<FavoriteEntry> matched = favorites.values().stream()
                .filter(favorite -> userId.equals(favorite.userId()))
                .filter(favorite -> serviceCatalogRepository.findById(favorite.serviceId()).isPresent())
                .sorted(Comparator.comparing(FavoriteEntry::createTime).reversed()
                        .thenComparing(Comparator.comparing(FavoriteEntry::favoriteId).reversed()))
                .toList();

        List<FavoriteItem> records = matched.stream()
                .skip(offset)
                .limit(limit)
                .map(favorite -> new FavoriteItem(
                        favorite.favoriteId(),
                        serviceCatalogRepository.findById(favorite.serviceId()).orElseThrow(),
                        favorite.sellerName(),
                        favorite.createTime()))
                .toList();
        return new FavoritePage(records, matched.size());
    }

    @Override
    public ServiceReviewPage findReviewsByService(Long serviceId, int offset, int limit) {
        List<ServiceReviewItem> matched = reviews.stream()
                .filter(review -> serviceId.equals(review.serviceId()))
                .sorted(Comparator.comparing(ServiceReviewItem::createTime).reversed()
                        .thenComparing(Comparator.comparing(ServiceReviewItem::id).reversed()))
                .toList();

        List<ServiceReviewItem> records = matched.stream()
                .skip(offset)
                .limit(limit)
                .toList();
        return new ServiceReviewPage(records, matched.size());
    }

    @Override
    public boolean existsReviewByOrderId(Long orderId) {
        return reviews.stream().anyMatch(review -> orderId.equals(review.orderId()));
    }

    private String favoriteKey(Long userId, Long serviceId) {
        return userId + ":" + serviceId;
    }

    private record FavoriteEntry(
            Long favoriteId,
            Long userId,
            Long serviceId,
            String sellerName,
            LocalDateTime createTime) {
    }
}
