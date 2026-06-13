package com.xueyifang.cloud.service.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.service.dto.ServiceReviewCreateRequest;
import com.xueyifang.cloud.service.dto.ServiceReviewListResponse;
import com.xueyifang.cloud.service.dto.ServiceReviewResponse;
import com.xueyifang.cloud.service.repository.ReviewableOrder;
import com.xueyifang.cloud.service.repository.ServiceCatalogRepository;
import com.xueyifang.cloud.service.repository.ServiceInteractionRepository;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.repository.ServiceReviewCreateCommand;
import com.xueyifang.cloud.service.repository.ServiceReviewPage;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceReviewService {

    private static final int ONLINE_STATUS = 1;

    private static final int ORDER_COMPLETED = 4;

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceCatalogRepository serviceCatalogRepository;

    private final ServiceInteractionRepository serviceInteractionRepository;

    public ServiceReviewService(ServiceCatalogRepository serviceCatalogRepository,
                                ServiceInteractionRepository serviceInteractionRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.serviceInteractionRepository = serviceInteractionRepository;
    }

    public ServiceReviewListResponse listServiceReviews(Long serviceId, Integer pageNum, Integer pageSize) {
        Long normalizedServiceId = requirePositiveId(serviceId, "serviceId");
        ServiceItem service = serviceCatalogRepository.findById(normalizedServiceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_NOT_EXIST));
        if (!canViewService(service)) {
            throw new BusinessException(ErrorCode.SERVICE_OFFLINE);
        }

        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        ServiceReviewPage page = serviceInteractionRepository.findReviewsByService(
                normalizedServiceId,
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize);

        int pages = calculatePages(page.total(), normalizedPageSize);
        List<ServiceReviewResponse> records = page.records().stream()
                .map(ServiceReviewResponse::from)
                .toList();
        return new ServiceReviewListResponse(
                records,
                page.total(),
                normalizedPageSize,
                normalizedPageNum,
                pages,
                normalizedPageNum,
                normalizedPageSize);
    }

    public boolean isOrderReviewed(Long orderId) {
        Long normalizedOrderId = requirePositiveId(orderId, "orderId");
        return serviceInteractionRepository.existsReviewByOrderId(normalizedOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createReview(ServiceReviewCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        LoginUserContext user = requireCurrentUser();
        Long orderId = requirePositiveId(request.orderId(), "orderId");
        int rating = normalizeRating(request.rating());
        String content = requireReviewContent(request.content());

        ReviewableOrder order = serviceInteractionRepository.findReviewableOrder(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXIST));
        if (!user.userId().equals(order.buyerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only the buyer can review the order");
        }
        if (!Integer.valueOf(ORDER_COMPLETED).equals(order.orderStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "only completed orders can be reviewed");
        }
        if (serviceInteractionRepository.existsReviewByOrderId(order.orderId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has already been reviewed");
        }

        Long reviewId;
        try {
            reviewId = serviceInteractionRepository.createReview(new ServiceReviewCreateCommand(
                    order.serviceId(),
                    order.orderId(),
                    order.buyerId(),
                    order.sellerId(),
                    rating,
                    content,
                    Boolean.TRUE.equals(request.anonymous())));
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "order has already been reviewed");
        }
        serviceInteractionRepository.refreshServiceRating(order.serviceId());
        return reviewId;
    }

    private boolean canViewService(ServiceItem service) {
        if (Integer.valueOf(ONLINE_STATUS).equals(service.status())) {
            return true;
        }

        return UserContextHolder.get()
                .map(user -> isAdmin(user) || user.userId().equals(service.publisherId()))
                .orElse(false);
    }

    private boolean isAdmin(LoginUserContext user) {
        return Integer.valueOf(ADMIN_ROLE).equals(user.role());
    }

    private LoginUserContext requireCurrentUser() {
        return UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
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

    private int normalizeRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "rating must be between 1 and 5");
        }
        return rating;
    }

    private String requireReviewContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "content must not be blank");
        }
        String normalized = content.trim();
        if (normalized.length() < 10 || normalized.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "content length must be between 10 and 500");
        }
        return normalized;
    }
}
