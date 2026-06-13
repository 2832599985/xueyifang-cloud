package com.xueyifang.cloud.service.repository;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository {

    ServicePage findServices(ServiceListQuery query);

    Optional<ServiceItem> findById(Long serviceId);

    List<ServiceImage> findImagesByServiceId(Long serviceId);

    List<ServiceTag> findActiveTags();
}
