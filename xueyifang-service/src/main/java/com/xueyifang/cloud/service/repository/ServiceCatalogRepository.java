package com.xueyifang.cloud.service.repository;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository {

    ServicePage findServices(ServiceListQuery query);

    Optional<ServiceItem> findById(Long serviceId);

    Long createService(ServiceCreateCommand command);

    boolean updateService(Long serviceId, ServiceUpdateCommand command);

    boolean updateServiceStatus(Long serviceId, int status, int reviewStatus);

    boolean updateServiceReview(Long serviceId, int status, int reviewStatus, String reviewReason, Long reviewedBy);

    boolean updateCoverImage(Long serviceId, String coverImage);

    boolean deleteService(Long serviceId);

    boolean incrementFavoriteCount(Long serviceId);

    boolean decrementFavoriteCount(Long serviceId);

    List<ServiceImage> findImagesByServiceId(Long serviceId);

    void insertImages(Long serviceId, List<String> imageUrls);

    void replaceImages(Long serviceId, List<String> imageUrls);

    List<ServiceTag> findActiveTags();
}
