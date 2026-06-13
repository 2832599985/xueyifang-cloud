package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.FavoriteCollectRequest;
import com.xueyifang.cloud.service.dto.FavoriteListItemResponse;
import com.xueyifang.cloud.service.dto.FavoriteListResponse;
import com.xueyifang.cloud.service.repository.FavoritePage;
import com.xueyifang.cloud.service.repository.ServiceCatalogRepository;
import com.xueyifang.cloud.service.repository.ServiceInteractionRepository;
import com.xueyifang.cloud.service.repository.ServiceItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceFavoriteService {

    private static final int ONLINE_STATUS = 1;

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceCatalogRepository serviceCatalogRepository;

    private final ServiceInteractionRepository serviceInteractionRepository;

    public ServiceFavoriteService(ServiceCatalogRepository serviceCatalogRepository,
                                  ServiceInteractionRepository serviceInteractionRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.serviceInteractionRepository = serviceInteractionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void collectService(FavoriteCollectRequest request) {
        LoginUserContext user = requireCurrentUser();
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }

        Long serviceId = requirePositiveId(request.serviceId(), "serviceId");
        ServiceItem service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_EXIST));
        if (!canViewService(user, service)) {
            throw new BusinessException(ErrorCode.SERVICE_OFFLINE);
        }

        if (serviceInteractionRepository.addFavorite(user.userId(), serviceId)) {
            serviceCatalogRepository.incrementFavoriteCount(serviceId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void uncollectService(Long serviceId) {
        LoginUserContext user = requireCurrentUser();
        Long normalizedServiceId = requirePositiveId(serviceId, "serviceId");

        if (serviceInteractionRepository.removeFavorite(user.userId(), normalizedServiceId)) {
            serviceCatalogRepository.decrementFavoriteCount(normalizedServiceId);
        }
    }

    public FavoriteListResponse listMyCollections(Integer pageNum, Integer pageSize) {
        LoginUserContext user = requireCurrentUser();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);

        FavoritePage page = serviceInteractionRepository.findFavoritesByUser(
                user.userId(),
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize);

        int pages = calculatePages(page.total(), normalizedPageSize);
        List<FavoriteListItemResponse> records = page.records().stream()
                .map(FavoriteListItemResponse::from)
                .toList();
        return new FavoriteListResponse(
                records,
                page.total(),
                normalizedPageSize,
                normalizedPageNum,
                pages,
                normalizedPageNum,
                normalizedPageSize);
    }

    public Boolean isCollectedByCurrentUser(Long serviceId) {
        return UserContextHolder.get()
                .map(user -> serviceInteractionRepository.existsFavorite(user.userId(), serviceId))
                .orElse(null);
    }

    private boolean canViewService(LoginUserContext user, ServiceItem service) {
        return Integer.valueOf(ONLINE_STATUS).equals(service.status())
                || isAdmin(user)
                || (user.userId() != null && user.userId().equals(service.publisherId()));
    }

    private LoginUserContext requireCurrentUser() {
        return UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
    }

    private boolean isAdmin(LoginUserContext user) {
        return Integer.valueOf(ADMIN_ROLE).equals(user.role());
    }

    private Long requirePositiveId(Long value, String name) {
        if (value == null || value <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + " must be positive");
        }
        return value;
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

    private int calculatePages(long total, int pageSize) {
        return total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }
}
