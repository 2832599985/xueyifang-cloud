package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.ServiceDetailResponse;
import com.xueyifang.cloud.service.dto.ServiceListResponse;
import com.xueyifang.cloud.service.dto.ServiceSummaryResponse;
import com.xueyifang.cloud.service.dto.ServiceTagResponse;
import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.repository.ServiceListQuery;
import com.xueyifang.cloud.service.repository.ServicePage;
import com.xueyifang.cloud.service.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCatalogService {

    private static final int ONLINE_STATUS = 1;

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public ServiceListResponse listServices(String keyword, Long tagId, Long categoryId, Long professionalId,
                                            Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        Integer effectiveStatus = canViewNonPublicServices() ? statusOrOnline(status) : ONLINE_STATUS;

        ServicePage page = serviceCatalogRepository.findServices(new ServiceListQuery(
                normalizeKeyword(keyword),
                positiveOrNull(tagId, "tagId"),
                positiveOrNull(categoryId, "categoryId"),
                positiveOrNull(professionalId, "professionalId"),
                effectiveStatus,
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize));

        int pages = page.total() == 0
                ? 0
                : (int) Math.ceil((double) page.total() / normalizedPageSize);
        List<ServiceSummaryResponse> records = page.records().stream()
                .map(ServiceSummaryResponse::from)
                .toList();
        return new ServiceListResponse(
                records,
                page.total(),
                normalizedPageNum,
                normalizedPageNum,
                normalizedPageSize,
                pages);
    }

    public ServiceDetailResponse getServiceDetail(Long serviceId) {
        if (serviceId == null || serviceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "serviceId must be positive");
        }

        ServiceItem service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_EXIST));
        if (!canViewService(service)) {
            throw new BusinessException(ErrorCode.SERVICE_OFFLINE);
        }

        List<ServiceImage> images = serviceCatalogRepository.findImagesByServiceId(serviceId);
        return ServiceDetailResponse.from(service, images);
    }

    public List<ServiceTagResponse> listTags() {
        return serviceCatalogRepository.findActiveTags().stream()
                .map(ServiceTagResponse::from)
                .toList();
    }

    private boolean canViewService(ServiceItem service) {
        if (Integer.valueOf(ONLINE_STATUS).equals(service.status())) {
            return true;
        }

        return UserContextHolder.get()
                .map(user -> isAdmin(user) || user.userId().equals(service.publisherId()))
                .orElse(false);
    }

    private boolean canViewNonPublicServices() {
        return UserContextHolder.get()
                .map(this::isAdmin)
                .orElse(false);
    }

    private boolean isAdmin(LoginUserContext user) {
        return Integer.valueOf(ADMIN_ROLE).equals(user.role());
    }

    private Integer statusOrOnline(Integer status) {
        return status != null ? status : ONLINE_STATUS;
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null) {
            return DEFAULT_PAGE_NUM;
        }
        if (pageNum < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageNum must be positive");
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private Long positiveOrNull(Long value, String name) {
        if (value == null) {
            return null;
        }
        if (value <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must be positive");
        }
        return value;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
