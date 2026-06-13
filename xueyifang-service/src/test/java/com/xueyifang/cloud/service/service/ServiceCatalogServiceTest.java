package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.ServiceDetailResponse;
import com.xueyifang.cloud.service.dto.ServiceListResponse;
import com.xueyifang.cloud.service.dto.ServiceTagResponse;
import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.support.InMemoryServiceCatalogRepository;
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

    private final ServiceCatalogService serviceCatalogService = new ServiceCatalogService(repository);

    @BeforeEach
    void setUp() {
        repository.putService(service(1L, 10L, "Java tutoring", 1, 1L));
        repository.putService(service(2L, 11L, "Dorm repair", 0, 2L));
        repository.putImage(new ServiceImage(1L, 1L, "cover.jpg", 0, true));
        repository.putImage(new ServiceImage(2L, 1L, "detail.jpg", 1, false));
        repository.putTag(1L, "study", 1, 1);
        repository.putTag(2L, "inactive", 2, 0);
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void listsOnlineServicesForAnonymousUsers() {
        ServiceListResponse response = serviceCatalogService.listServices(
                null, null, null, null, null, null, null);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records()).hasSize(1);
        assertThat(response.records().getFirst().serviceId()).isEqualTo(1L);
        assertThat(response.pageNum()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(10);
    }

    @Test
    void filtersByKeywordAndTag() {
        ServiceListResponse response = serviceCatalogService.listServices(
                "java", 1L, null, null, null, 1, 20);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records().getFirst().title()).isEqualTo("Java tutoring");
    }

    @Test
    void getsOnlineServiceDetailWithImages() {
        ServiceDetailResponse response = serviceCatalogService.getServiceDetail(1L);

        assertThat(response.serviceId()).isEqualTo(1L);
        assertThat(response.images()).extracting("imageUrl")
                .containsExactly("cover.jpg", "detail.jpg");
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
                null, null, null, null, 0, 1, 10);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.records().getFirst().serviceId()).isEqualTo(2L);
    }

    @Test
    void listsActiveTagsOnly() {
        List<ServiceTagResponse> tags = serviceCatalogService.listTags();

        assertThat(tags).hasSize(1);
        assertThat(tags.getFirst().name()).isEqualTo("study");
    }

    @Test
    void rejectsInvalidPagination() {
        assertThatThrownBy(() -> serviceCatalogService.listServices(
                null, null, null, null, null, 0, 10))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.PARAMS_ERROR.getCode()));
    }

    private ServiceItem service(Long id, Long publisherId, String title, Integer status, Long tagId) {
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
                1,
                0,
                0,
                BigDecimal.ZERO,
                "cover.jpg",
                LocalDateTime.parse("2026-06-14T00:00:00"),
                LocalDateTime.parse("2026-06-14T00:00:00"));
    }
}
