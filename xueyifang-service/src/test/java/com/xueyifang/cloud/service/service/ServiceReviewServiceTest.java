package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.ServiceReviewCreateRequest;
import com.xueyifang.cloud.service.dto.ServiceReviewListResponse;
import com.xueyifang.cloud.service.repository.ReviewableOrder;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.repository.ServiceReviewItem;
import com.xueyifang.cloud.service.support.InMemoryServiceCatalogRepository;
import com.xueyifang.cloud.service.support.InMemoryServiceInteractionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceReviewServiceTest {

    private final InMemoryServiceCatalogRepository catalogRepository = new InMemoryServiceCatalogRepository();

    private final InMemoryServiceInteractionRepository interactionRepository =
            new InMemoryServiceInteractionRepository(catalogRepository);

    private final ServiceReviewService reviewService =
            new ServiceReviewService(catalogRepository, interactionRepository);

    @BeforeEach
    void setUp() {
        catalogRepository.putService(service(1L, 10L, "Java tutoring", 1));
        catalogRepository.putService(service(2L, 11L, "Dorm repair", 0));
        interactionRepository.clearInteractions();
        interactionRepository.putReview(new ServiceReviewItem(
                1L,
                1L,
                100L,
                12L,
                10L,
                5,
                "Very helpful tutoring.",
                false,
                LocalDateTime.parse("2026-06-14T01:00:00"),
                "Bob",
                "bob.png"));
        interactionRepository.putReview(new ServiceReviewItem(
                2L,
                1L,
                101L,
                13L,
                10L,
                4,
                "Anonymous but solid service.",
                true,
                LocalDateTime.parse("2026-06-14T02:00:00"),
                "Carol",
                "carol.png"));
        interactionRepository.putReviewableOrder(new ReviewableOrder(200L, 1L, 12L, 10L, 4));
        interactionRepository.putReviewableOrder(new ReviewableOrder(201L, 1L, 12L, 10L, 3));
        interactionRepository.putReviewableOrder(new ReviewableOrder(202L, 1L, 13L, 10L, 4));
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void listsPublicServiceReviewsAndHidesAnonymousReviewer() {
        ServiceReviewListResponse response = reviewService.listServiceReviews(1L, 1, 10);

        assertThat(response.total()).isEqualTo(2);
        assertThat(response.records()).hasSize(2);
        assertThat(response.records().getFirst().id()).isEqualTo(2L);
        assertThat(response.records().getFirst().reviewerId()).isNull();
        assertThat(response.records().getFirst().reviewerName()).isEqualTo("匿名用户");
        assertThat(response.records().get(1).reviewerName()).isEqualTo("Bob");
    }

    @Test
    void rejectsOfflineServiceReviewsForAnonymousUsers() {
        assertThatThrownBy(() -> reviewService.listServiceReviews(2L, 1, 10))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_OFFLINE.getCode()));
    }

    @Test
    void allowsPublisherToViewOfflineServiceReviews() {
        UserContextHolder.set(new LoginUserContext(11L, 1, 1));

        ServiceReviewListResponse response = reviewService.listServiceReviews(2L, 1, 10);

        assertThat(response.total()).isZero();
    }

    @Test
    void checksOrderReviewStatus() {
        assertThat(reviewService.isOrderReviewed(100L)).isTrue();
        assertThat(reviewService.isOrderReviewed(999L)).isFalse();
    }

    @Test
    void createsReviewForCompletedBuyerOrder() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        Long reviewId = reviewService.createReview(new ServiceReviewCreateRequest(
                200L,
                5,
                "Really useful service.",
                true));

        assertThat(reviewId).isEqualTo(3L);
        assertThat(reviewService.isOrderReviewed(200L)).isTrue();
        ServiceReviewListResponse response = reviewService.listServiceReviews(1L, 1, 10);
        assertThat(response.total()).isEqualTo(3);
        assertThat(response.records().getFirst().reviewerName()).isEqualTo("匿名用户");
    }

    @Test
    void rejectsReviewWhenOrderIsNotCompletedOrBuyerDoesNotMatch() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        assertThatThrownBy(() -> reviewService.createReview(new ServiceReviewCreateRequest(
                201L,
                5,
                "Not completed yet.",
                false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.ORDER_STATUS_ERROR.getCode()));

        assertThatThrownBy(() -> reviewService.createReview(new ServiceReviewCreateRequest(
                202L,
                5,
                "This belongs to another buyer.",
                false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.NO_AUTH_ERROR.getCode()));
    }

    @Test
    void rejectsDuplicateReview() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        assertThatThrownBy(() -> reviewService.createReview(new ServiceReviewCreateRequest(
                100L,
                5,
                "Already reviewed order.",
                false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.ORDER_NOT_EXIST.getCode()));

        interactionRepository.putReviewableOrder(new ReviewableOrder(100L, 1L, 12L, 10L, 4));
        assertThatThrownBy(() -> reviewService.createReview(new ServiceReviewCreateRequest(
                100L,
                5,
                "Already reviewed order.",
                false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.OPERATION_ERROR.getCode()));
    }

    @Test
    void rejectsInvalidPagination() {
        assertThatThrownBy(() -> reviewService.listServiceReviews(1L, 0, 10))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void rejectsInvalidReviewContent() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        assertThatThrownBy(() -> reviewService.createReview(new ServiceReviewCreateRequest(
                200L,
                5,
                "short",
                false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    private ServiceItem service(Long id, Long publisherId, String title, Integer status) {
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
                0,
                0,
                BigDecimal.ZERO,
                "cover.jpg",
                LocalDateTime.parse("2026-06-14T00:00:00"),
                LocalDateTime.parse("2026-06-14T00:00:00"));
    }
}
