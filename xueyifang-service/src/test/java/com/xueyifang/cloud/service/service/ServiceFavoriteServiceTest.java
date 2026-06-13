package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.FavoriteCollectRequest;
import com.xueyifang.cloud.service.dto.FavoriteListResponse;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.support.InMemoryServiceCatalogRepository;
import com.xueyifang.cloud.service.support.InMemoryServiceInteractionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceFavoriteServiceTest {

    private final InMemoryServiceCatalogRepository catalogRepository = new InMemoryServiceCatalogRepository();

    private final InMemoryServiceInteractionRepository interactionRepository =
            new InMemoryServiceInteractionRepository(catalogRepository);

    private final ServiceFavoriteService favoriteService =
            new ServiceFavoriteService(catalogRepository, interactionRepository);

    @BeforeEach
    void setUp() {
        catalogRepository.putService(service(1L, 10L, "Java tutoring", 1, 0));
        catalogRepository.putService(service(2L, 11L, "Dorm repair", 0, 0));
        interactionRepository.clearInteractions();
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void collectsServiceIdempotentlyAndUpdatesCountOnce() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        favoriteService.collectService(new FavoriteCollectRequest(1L));
        favoriteService.collectService(new FavoriteCollectRequest(1L));

        assertThat(interactionRepository.existsFavorite(12L, 1L)).isTrue();
        assertThat(catalogRepository.findById(1L).orElseThrow().favoriteCount()).isEqualTo(1);
    }

    @Test
    void uncollectsServiceIdempotentlyAndDoesNotMakeCountNegative() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));
        favoriteService.collectService(new FavoriteCollectRequest(1L));

        favoriteService.uncollectService(1L);
        favoriteService.uncollectService(1L);

        assertThat(interactionRepository.existsFavorite(12L, 1L)).isFalse();
        assertThat(catalogRepository.findById(1L).orElseThrow().favoriteCount()).isZero();
    }

    @Test
    void listsCurrentUserCollections() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));
        interactionRepository.putFavorite(
                9L,
                12L,
                1L,
                "Alice",
                LocalDateTime.parse("2026-06-14T01:00:00"));

        FavoriteListResponse response = favoriteService.listMyCollections(1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).hasSize(1);
        assertThat(response.records().getFirst().favoriteId()).isEqualTo(9L);
        assertThat(response.records().getFirst().sellerName()).isEqualTo("Alice");
        assertThat(response.records().getFirst().service().serviceId()).isEqualTo(1L);
    }

    @Test
    void rejectsAnonymousCollect() {
        assertThatThrownBy(() -> favoriteService.collectService(new FavoriteCollectRequest(1L)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NOT_LOGIN.getCode()));
    }

    @Test
    void rejectsCollectingInvisibleOfflineService() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        assertThatThrownBy(() -> favoriteService.collectService(new FavoriteCollectRequest(2L)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_OFFLINE.getCode()));
    }

    private ServiceItem service(Long id, Long publisherId, String title, Integer status, Integer favoriteCount) {
        return new ServiceItem(
                id,
                publisherId,
                title,
                "description " + title,
                1L,
                "study",
                1L,
                "category",
                1L,
                "computer science",
                BigDecimal.valueOf(20),
                "hour",
                "library",
                status,
                1,
                favoriteCount,
                0,
                BigDecimal.ZERO,
                "cover.jpg",
                LocalDateTime.parse("2026-06-14T00:00:00"),
                LocalDateTime.parse("2026-06-14T00:00:00"));
    }
}
