package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.AdminServiceReviewRequest;
import com.xueyifang.cloud.service.dto.PageResponse;
import com.xueyifang.cloud.service.dto.PendingServiceReviewResponse;
import com.xueyifang.cloud.service.dto.ServiceDetailResponse;
import com.xueyifang.cloud.service.dto.ServiceListResponse;
import com.xueyifang.cloud.service.dto.ServicePublishRequest;
import com.xueyifang.cloud.service.dto.ServiceReviewDecisionResponse;
import com.xueyifang.cloud.service.dto.ServiceTagResponse;
import com.xueyifang.cloud.service.dto.ServiceUpdateRequest;
import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.support.InMemoryServiceCatalogRepository;
import com.xueyifang.cloud.service.support.InMemoryServiceInteractionRepository;
import com.xueyifang.cloud.service.support.MutableServiceReviewModeRepository;
import com.xueyifang.cloud.service.support.RecordingServiceNotificationPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceCatalogServiceTest {

    private final InMemoryServiceCatalogRepository repository = new InMemoryServiceCatalogRepository();

    private final InMemoryServiceInteractionRepository interactionRepository =
            new InMemoryServiceInteractionRepository(repository);

    private final MutableServiceReviewModeRepository reviewModeRepository =
            new MutableServiceReviewModeRepository(false);

    private final RecordingServiceNotificationPublisher notificationPublisher =
            new RecordingServiceNotificationPublisher();

    private final ServiceCatalogService serviceCatalogService = new ServiceCatalogService(
            repository,
            interactionRepository,
            reviewModeRepository,
            notificationPublisher);

    @BeforeEach
    void setUp() {
        repository.putService(service(1L, 10L, "Java tutoring", 1, 1L));
        repository.putService(service(2L, 11L, "Dorm repair", 0, 2L));
        repository.putService(service(3L, 12L, "Math tutoring", 2, 0, 1L));
        repository.putImage(new ServiceImage(1L, 1L, "cover.jpg", 0, true));
        repository.putImage(new ServiceImage(2L, 1L, "detail.jpg", 1, false));
        repository.putTag(1L, "study", 1, 1);
        repository.putTag(2L, "inactive", 2, 0);
        interactionRepository.clearInteractions();
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void listsOnlineServicesForAnonymousUsers() {
        ServiceListResponse response = serviceCatalogService.listServices(
                null, null, null, null, null, null, null, null);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).hasSize(1);
        assertThat(response.records().getFirst().serviceId()).isEqualTo(1L);
        assertThat(response.pageNum()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(10);
    }

    @Test
    void filtersByKeywordAndTag() {
        ServiceListResponse response = serviceCatalogService.listServices(
                "java", 1L, null, null, null, null, 1, 20);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records().getFirst().title()).isEqualTo("Java tutoring");
    }

    @Test
    void getsOnlineServiceDetailWithImages() {
        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(1L);

        assertThat(response.serviceId()).isEqualTo(1L);
        assertThat(response.images()).extracting("imageUrl")
                .containsExactly("cover.jpg", "detail.jpg");
        assertThat(response.isCollected()).isNull();
    }

    @Test
    void includesCollectionStateForLoggedInUsers() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));
        interactionRepository.addFavorite(10L, 1L);

        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(1L);

        assertThat(response.isCollected()).isTrue();
    }

    @Test
    void rejectsOfflineServiceDetailForAnonymousUsers() {
        assertThatThrownBy(() -> serviceCatalogService.getServiceDetail(2L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_OFFLINE.getCode()));
    }

    @Test
    void allowsPublisherToViewOwnOfflineService() {
        UserContextHolder.set(new LoginUserContext(11L, 1, 1));

        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(2L);

        assertThat(response.serviceId()).isEqualTo(2L);
        assertThat(response.status()).isZero();
    }

    @Test
    void allowsAdminToListOfflineServicesWhenStatusSpecified() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        ServiceListResponse response = serviceCatalogService.listServices(
                null, null, null, null, null, 0, 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records().getFirst().serviceId()).isEqualTo(2L);
    }

    @Test
    void listsActiveTagsOnly() {
        List<ServiceTagResponse> tags = serviceCatalogService.listTags();

        assertThat(tags).hasSize(1);
        assertThat(tags.getFirst().tagName()).isEqualTo("study");
    }

    @Test
    void rejectsInvalidPagination() {
        assertThatThrownBy(() -> serviceCatalogService.listServices(
                null, null, null, null, null, null, 0, 10))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    @Test
    void publishesServiceForUserWithPermission() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        Long serviceId = serviceCatalogService.publishService(publishRequest());
        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(serviceId);

        assertThat(response.publisherId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Published service");
        assertThat(response.status()).isEqualTo(1);
        assertThat(response.coverImage()).isEqualTo("published-cover.jpg");
        assertThat(response.images()).extracting("imageUrl")
                .containsExactly("published-cover.jpg", "published-detail.jpg");
    }

    @Test
    void publishesServiceAsReviewingWhenReviewModeEnabled() {
        reviewModeRepository.setRequiresReview(true);
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        Long serviceId = serviceCatalogService.publishService(publishRequest());
        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(serviceId);

        assertThat(response.status()).isEqualTo(2);
        assertThat(response.reviewStatus()).isZero();
    }

    @Test
    void rejectsPublishWithoutPermission() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 0));

        assertThatThrownBy(() -> serviceCatalogService.publishService(publishRequest()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.USER_NO_PUBLISH_PERMISSION.getCode()));
    }

    @Test
    void listsMyServicesForCurrentPublisher() {
        UserContextHolder.set(new LoginUserContext(11L, 1, 1));

        ServiceListResponse response = serviceCatalogService.listMyServices(null, 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records().getFirst().serviceId()).isEqualTo(2L);
    }

    @Test
    void updatesOfflineServiceForOwnerAndReplacesImages() {
        UserContextHolder.set(new LoginUserContext(11L, 1, 1));

        serviceCatalogService.updateService(2L, updateRequest());
        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(2L);

        assertThat(response.title()).isEqualTo("Updated service");
        assertThat(response.price()).isEqualByComparingTo("35.00");
        assertThat(response.coverImage()).isEqualTo("updated-cover.jpg");
        assertThat(response.images()).extracting("imageUrl")
                .containsExactly("updated-cover.jpg", "updated-detail.jpg");
    }

    @Test
    void rejectsEditingOnlineService() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        assertThatThrownBy(() -> serviceCatalogService.updateService(1L, updateRequest()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_CANNOT_EDIT.getCode()));
    }

    @Test
    void takesServiceOfflineAndOnlineAgain() {
        UserContextHolder.set(new LoginUserContext(10L, 1, 1));

        serviceCatalogService.offlineService(1L);
        assertThat(serviceCatalogService.getServiceDetail(1L).status()).isZero();

        serviceCatalogService.onlineService(1L);
        assertThat(serviceCatalogService.getServiceDetail(1L).status()).isEqualTo(1);
    }

    @Test
    void resubmitsOfflineServiceForReviewWhenReviewModeEnabled() {
        reviewModeRepository.setRequiresReview(true);
        UserContextHolder.set(new LoginUserContext(11L, 1, 1));

        serviceCatalogService.onlineService(2L);

        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(2L);
        assertThat(response.status()).isEqualTo(2);
        assertThat(response.reviewStatus()).isZero();
    }

    @Test
    void listsPendingServicesForAdmin() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        PageResponse<PendingServiceReviewResponse> page = serviceCatalogService.listPendingReviewServices(1, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records().getFirst().serviceId()).isEqualTo(3L);
    }

    @Test
    void approvesPendingServiceAndPublishesNotification() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        ServiceReviewDecisionResponse response = serviceCatalogService.reviewService(
                new AdminServiceReviewRequest(3L, true, null));

        assertThat(response.status()).isEqualTo("approved");
        assertThat(serviceCatalogService.getServiceDetail(3L).status()).isEqualTo(1);
        assertThat(notificationPublisher.notifications()).hasSize(1);
        assertThat(notificationPublisher.notifications().getFirst().recipientId()).isEqualTo(12L);
        assertThat(notificationPublisher.notifications().getFirst().serviceId()).isEqualTo(3L);
    }

    @Test
    void rejectsPendingServiceWithReason() {
        UserContextHolder.set(new LoginUserContext(99L, 2, 1));

        ServiceReviewDecisionResponse response = serviceCatalogService.reviewService(
                new AdminServiceReviewRequest(3L, false, "描述不清晰"));

        assertThat(response.status()).isEqualTo("rejected");
        assertThat(serviceCatalogService.getServiceDetail(3L).status()).isEqualTo(3);
        assertThat(notificationPublisher.notifications().getFirst().content()).contains("描述不清晰");
    }

    @Test
    void rejectsStatusChangeByNonOwner() {
        UserContextHolder.set(new LoginUserContext(12L, 1, 1));

        assertThatThrownBy(() -> serviceCatalogService.offlineService(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_NOT_OWNER.getCode()));
    }

    @Test
    void deletesOfflineServiceForOwner() {
        UserContextHolder.set(new LoginUserContext(11L, 1, 1));

        serviceCatalogService.deleteService(2L);

        assertThatThrownBy(() -> serviceCatalogService.getServiceDetail(2L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.SERVICE_NOT_EXIST.getCode()));
    }

    private ServiceItem service(Long id, Long publisherId, String title, Integer status, Long tagId) {
        return service(id, publisherId, title, status, 1, tagId);
    }

    private ServiceItem service(Long id, Long publisherId, String title, Integer status,
                                Integer reviewStatus, Long tagId) {
        return new ServiceItem(
                id,
                publisherId,
                title,
                "description " + title,
                tagId,
                "study",
                1L,
                "category",
                1L,
                "computer science",
                BigDecimal.valueOf(20),
                "hour",
                "library",
                status,
                reviewStatus,
                0,
                0,
                BigDecimal.ZERO,
                "cover.jpg",
                LocalDateTime.parse("2026-06-14T00:00:00"),
                LocalDateTime.parse("2026-06-14T00:00:00"));
    }

    private ServicePublishRequest publishRequest() {
        return new ServicePublishRequest(
                null,
                "Published service",
                null,
                "description Published service",
                1L,
                "study",
                1L,
                "category",
                1L,
                "computer science",
                BigDecimal.valueOf(25),
                "hour",
                "library",
                null,
                List.of("published-cover.jpg", "published-detail.jpg"),
                null,
                null,
                null,
                null);
    }

    private ServiceUpdateRequest updateRequest() {
        return new ServiceUpdateRequest(
                "Updated service",
                null,
                "description Updated service",
                null,
                1L,
                "study",
                1L,
                "category",
                1L,
                "computer science",
                BigDecimal.valueOf(35),
                "hour",
                "lab",
                null,
                List.of("updated-cover.jpg", "updated-detail.jpg"),
                null,
                null,
                null,
                null);
    }
}
