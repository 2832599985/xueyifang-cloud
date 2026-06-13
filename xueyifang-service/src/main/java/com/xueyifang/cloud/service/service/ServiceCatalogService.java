package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.ServiceDetailResponse;
import com.xueyifang.cloud.service.dto.ServiceListResponse;
import com.xueyifang.cloud.service.dto.ServicePublishRequest;
import com.xueyifang.cloud.service.dto.ServiceSummaryResponse;
import com.xueyifang.cloud.service.dto.ServiceTagResponse;
import com.xueyifang.cloud.service.dto.ServiceUpdateRequest;
import com.xueyifang.cloud.service.repository.ServiceCreateCommand;
import com.xueyifang.cloud.service.repository.ServiceCatalogRepository;
import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.repository.ServiceListQuery;
import com.xueyifang.cloud.service.repository.ServicePage;
import com.xueyifang.cloud.service.repository.ServiceUpdateCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServiceCatalogService {

    private static final int OFFLINE_STATUS = 0;

    private static final int ONLINE_STATUS = 1;

    private static final int REVIEWING_STATUS = 2;

    private static final int REJECTED_STATUS = 3;

    private static final int REVIEW_APPROVED = 1;

    private static final int ADMIN_ROLE = 2;

    private static final int HAS_PUBLISH_PERMISSION = 1;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public ServiceListResponse listServices(String keyword, Long tagId, Long categoryId, Long professionalId,
                                            Long publisherId, Integer status, Integer pageNum, Integer pageSize) {
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        Integer effectiveStatus = canViewNonPublicServices() ? statusOrOnline(status) : ONLINE_STATUS;

        ServicePage page = serviceCatalogRepository.findServices(new ServiceListQuery(
                normalizeKeyword(keyword),
                positiveOrNull(tagId, "tagId"),
                positiveOrNull(categoryId, "categoryId"),
                positiveOrNull(professionalId, "professionalId"),
                positiveOrNull(publisherId, "publisherId"),
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

    public ServiceListResponse listMyServices(Integer status, Integer pageNum, Integer pageSize) {
        LoginUserContext user = requireCurrentUser();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);

        ServicePage page = serviceCatalogRepository.findServices(new ServiceListQuery(
                null,
                null,
                null,
                null,
                user.userId(),
                normalizeStatusOrNull(status),
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

    @Transactional(rollbackFor = Exception.class)
    public Long publishService(ServicePublishRequest request) {
        LoginUserContext user = requireCurrentUser();
        if (!canPublish(user)) {
            throw new BusinessException(ErrorCode.USER_NO_PUBLISH_PERMISSION);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }

        List<String> imageUrls = normalizeImages(request.images());
        String coverImage = firstNonBlank(normalizeOptional(request.coverImage()), firstImageOrNull(imageUrls));

        Long serviceId = serviceCatalogRepository.createService(new ServiceCreateCommand(
                user.userId(),
                requireText(firstNonBlank(request.title(), request.serviceTitle()), "title"),
                requireText(firstNonBlank(request.description(), request.serviceDescription()), "description"),
                positiveOrNull(request.tagId(), "tagId"),
                normalizeOptional(request.tagName()),
                positiveOrNull(request.categoryId(), "categoryId"),
                normalizeOptional(request.categoryName()),
                positiveOrNull(request.professionalId(), "professionalId"),
                normalizeOptional(request.professionalName()),
                requirePositivePrice(request.price()),
                normalizeOptional(request.unit()),
                normalizeOptional(request.location()),
                ONLINE_STATUS,
                REVIEW_APPROVED,
                coverImage));
        serviceCatalogRepository.insertImages(serviceId, imageUrls);
        return serviceId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateService(Long serviceId, ServiceUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        LoginUserContext user = requireCurrentUser();
        ServiceItem service = getMutableService(serviceId);
        ensureCanManage(user, service);
        if (Integer.valueOf(ONLINE_STATUS).equals(service.status())) {
            throw new BusinessException(ErrorCode.SERVICE_CANNOT_EDIT, "online service must be offline before edit");
        }

        ServiceUpdateCommand command = toUpdateCommand(request);
        if (command.hasChanges() && !serviceCatalogRepository.updateService(serviceId, command)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "service update failed");
        }

        if (request.images() != null) {
            List<String> imageUrls = normalizeImages(request.images());
            serviceCatalogRepository.replaceImages(serviceId, imageUrls);
            String coverImage = firstNonBlank(normalizeOptional(request.coverImage()), firstImageOrNull(imageUrls));
            if (!serviceCatalogRepository.updateCoverImage(serviceId, coverImage)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "cover image update failed");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void offlineService(Long serviceId) {
        LoginUserContext user = requireCurrentUser();
        ServiceItem service = getMutableService(serviceId);
        ensureCanManage(user, service);
        if (!Integer.valueOf(ONLINE_STATUS).equals(service.status())) {
            throw new BusinessException(ErrorCode.SERVICE_CANNOT_OFFLINE,
                    "only online services can be taken offline");
        }
        if (!serviceCatalogRepository.updateServiceStatus(serviceId, OFFLINE_STATUS, REVIEW_APPROVED)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "service offline failed");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void onlineService(Long serviceId) {
        LoginUserContext user = requireCurrentUser();
        ServiceItem service = getMutableService(serviceId);
        ensureCanManage(user, service);
        if (!Integer.valueOf(OFFLINE_STATUS).equals(service.status())
                && !Integer.valueOf(REJECTED_STATUS).equals(service.status())) {
            throw new BusinessException(ErrorCode.SERVICE_CANNOT_ONLINE,
                    "only offline or rejected services can be put online");
        }
        if (!serviceCatalogRepository.updateServiceStatus(serviceId, ONLINE_STATUS, REVIEW_APPROVED)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "service online failed");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteService(Long serviceId) {
        LoginUserContext user = requireCurrentUser();
        ServiceItem service = getMutableService(serviceId);
        ensureCanManage(user, service);
        if (Integer.valueOf(ONLINE_STATUS).equals(service.status())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "online service must be offline before delete");
        }
        if (!serviceCatalogRepository.deleteService(serviceId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "service delete failed");
        }
        serviceCatalogRepository.replaceImages(serviceId, List.of());
    }

    public List<ServiceTagResponse> listTags() {
        return serviceCatalogRepository.findActiveTags().stream()
                .map(ServiceTagResponse::from)
                .toList();
    }

    private ServiceUpdateCommand toUpdateCommand(ServiceUpdateRequest request) {
        return new ServiceUpdateCommand(
                normalizeUpdateText(firstNonNull(request.title(), request.serviceTitle()), "title"),
                normalizeUpdateText(firstNonNull(request.description(), request.serviceDescription()), "description"),
                positiveOrNull(request.tagId(), "tagId"),
                normalizeOptional(request.tagName()),
                positiveOrNull(request.categoryId(), "categoryId"),
                normalizeOptional(request.categoryName()),
                positiveOrNull(request.professionalId(), "professionalId"),
                normalizeOptional(request.professionalName()),
                normalizePositivePrice(request.price()),
                normalizeOptional(request.unit()),
                normalizeOptional(request.location()),
                request.images() == null ? normalizeOptional(request.coverImage()) : null);
    }

    private ServiceItem getMutableService(Long serviceId) {
        if (serviceId == null || serviceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "serviceId must be positive");
        }
        return serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_EXIST));
    }

    private void ensureCanManage(LoginUserContext user, ServiceItem service) {
        if (!isAdmin(user) && (user.userId() == null || !user.userId().equals(service.publisherId()))) {
            throw new BusinessException(ErrorCode.SERVICE_NOT_OWNER);
        }
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

    private LoginUserContext requireCurrentUser() {
        return UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
    }

    private boolean isAdmin(LoginUserContext user) {
        return Integer.valueOf(ADMIN_ROLE).equals(user.role());
    }

    private boolean canPublish(LoginUserContext user) {
        return isAdmin(user) || Integer.valueOf(HAS_PUBLISH_PERMISSION).equals(user.publishPermission());
    }

    private Integer statusOrOnline(Integer status) {
        return status != null ? normalizeStatus(status) : ONLINE_STATUS;
    }

    private Integer normalizeStatusOrNull(Integer status) {
        return status != null ? normalizeStatus(status) : null;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == OFFLINE_STATUS || status == ONLINE_STATUS
                || status == REVIEWING_STATUS || status == REJECTED_STATUS) {
            return status;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "status is invalid");
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

    private BigDecimal requirePositivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "price must be greater than 0");
        }
        return price;
    }

    private BigDecimal normalizePositivePrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return requirePositivePrice(price);
    }

    private String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeUpdateText(String value, String name) {
        if (value == null) {
            return null;
        }
        return requireText(value, name);
    }

    private List<String> normalizeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .map(this::normalizeOptional)
                .filter(value -> value != null)
                .toList();
    }

    private String firstImageOrNull(List<String> images) {
        return images.isEmpty() ? null : images.getFirst();
    }

    private String firstNonNull(String primary, String fallback) {
        return primary != null ? primary : fallback;
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
