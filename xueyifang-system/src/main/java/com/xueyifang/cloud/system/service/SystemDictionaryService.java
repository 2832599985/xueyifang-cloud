package com.xueyifang.cloud.system.service;

import com.xueyifang.cloud.common.core.api.ErrorCode;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import com.xueyifang.cloud.common.core.exception.BusinessException;
import com.xueyifang.cloud.system.dto.PageResponse;
import com.xueyifang.cloud.system.dto.ProfessionalRequest;
import com.xueyifang.cloud.system.dto.ProfessionalResponse;
import com.xueyifang.cloud.system.dto.SysConfigRegisterStatusResponse;
import com.xueyifang.cloud.system.dto.SysConfigResponse;
import com.xueyifang.cloud.system.dto.SysConfigUpdateRequest;
import com.xueyifang.cloud.system.dto.TradeLocationRequest;
import com.xueyifang.cloud.system.dto.TradeLocationResponse;
import com.xueyifang.cloud.system.repository.ProfessionalCommand;
import com.xueyifang.cloud.system.repository.ProfessionalItem;
import com.xueyifang.cloud.system.repository.SysConfigItem;
import com.xueyifang.cloud.system.repository.SysConfigUpdateCommand;
import com.xueyifang.cloud.system.repository.SystemDictionaryRepository;
import com.xueyifang.cloud.system.repository.SystemPage;
import com.xueyifang.cloud.system.repository.TradeLocationCommand;
import com.xueyifang.cloud.system.repository.TradeLocationItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SystemDictionaryService {

    private static final int ADMIN_ROLE = 2;

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final int MAX_PAGE_SIZE = 100;

    private static final String REGISTER_ENABLED_KEY = "REGISTER_ENABLED";

    private static final String REGISTER_ENABLED_DEFAULT = "1";

    private final SystemDictionaryRepository repository;

    public SystemDictionaryService(SystemDictionaryRepository repository) {
        this.repository = repository;
    }

    public List<ProfessionalResponse> listProfessionals() {
        return repository.findProfessionals().stream()
                .map(ProfessionalResponse::from)
                .toList();
    }

    public ProfessionalResponse getProfessional(Long id) {
        return ProfessionalResponse.from(findProfessional(id));
    }

    public PageResponse<ProfessionalResponse> pageProfessionals(String nameLike, Integer pageNum, Integer pageSize) {
        requireAdmin();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        SystemPage<ProfessionalItem> page = repository.findProfessionals(
                normalizeOptional(nameLike),
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize);
        return PageResponse.of(
                page.records().stream().map(ProfessionalResponse::from).toList(),
                page.total(),
                normalizedPageNum,
                normalizedPageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createProfessional(ProfessionalRequest request) {
        requireAdmin();
        ProfessionalCommand command = toProfessionalCommand(request);
        if (repository.countProfessionalsByName(command.professionalName(), null) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "professional name already exists");
        }
        return repository.createProfessional(command);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfessional(ProfessionalRequest request) {
        requireAdmin();
        if (request == null || request.id() == null || request.id() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id must be positive");
        }
        findProfessional(request.id());
        ProfessionalCommand command = toProfessionalCommand(request);
        if (repository.countProfessionalsByName(command.professionalName(), request.id()) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "professional name already exists");
        }
        if (!repository.updateProfessional(request.id(), command)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "professional update failed");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProfessional(Long id) {
        requireAdmin();
        findProfessional(id);
        if (!repository.deleteProfessional(id)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "professional delete failed");
        }
    }

    public List<TradeLocationResponse> listTradeLocations(boolean availableOnly) {
        return repository.findTradeLocations(availableOnly).stream()
                .map(TradeLocationResponse::from)
                .toList();
    }

    public TradeLocationResponse getTradeLocation(Long id) {
        return TradeLocationResponse.from(findTradeLocation(id));
    }

    public PageResponse<TradeLocationResponse> pageTradeLocations(Integer isAvailable, String nameLike,
                                                                  Integer pageNum, Integer pageSize,
                                                                  boolean adminOnly) {
        if (adminOnly) {
            requireAdmin();
        }
        Integer normalizedAvailable = normalizeAvailableOrNull(isAvailable);
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        SystemPage<TradeLocationItem> page = repository.findTradeLocations(
                normalizedAvailable,
                normalizeOptional(nameLike),
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize);
        return PageResponse.of(
                page.records().stream().map(TradeLocationResponse::from).toList(),
                page.total(),
                normalizedPageNum,
                normalizedPageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createTradeLocation(TradeLocationRequest request) {
        requireAdmin();
        TradeLocationCommand command = toTradeLocationCommand(request, true);
        if (repository.countTradeLocationsByName(command.locationName(), null) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "trade location name already exists");
        }
        return repository.createTradeLocation(command);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTradeLocation(TradeLocationRequest request) {
        requireAdmin();
        if (request == null || request.id() == null || request.id() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id must be positive");
        }
        findTradeLocation(request.id());
        TradeLocationCommand command = toTradeLocationCommand(request, false);
        if (repository.countTradeLocationsByName(command.locationName(), request.id()) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "trade location name already exists");
        }
        if (!repository.updateTradeLocation(request.id(), command)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "trade location update failed");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTradeLocation(Long id) {
        requireAdmin();
        findTradeLocation(id);
        if (!repository.deleteTradeLocation(id)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "trade location delete failed");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteTradeLocations(List<Long> ids) {
        requireAdmin();
        List<Long> normalizedIds = normalizeIds(ids);
        return repository.deleteTradeLocations(normalizedIds);
    }

    public SysConfigRegisterStatusResponse getRegisterStatus() {
        String value = repository.findEnabledConfigByKey(REGISTER_ENABLED_KEY)
                .map(SysConfigItem::configValue)
                .orElse(REGISTER_ENABLED_DEFAULT);
        return new SysConfigRegisterStatusResponse(isTruthy(value));
    }

    public PageResponse<SysConfigResponse> pageSysConfigs(String keyLike, Integer pageNum, Integer pageSize) {
        requireAdmin();
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        SystemPage<SysConfigItem> page = repository.findSysConfigs(
                normalizeOptional(keyLike),
                (normalizedPageNum - 1) * normalizedPageSize,
                normalizedPageSize);
        return PageResponse.of(
                page.records().stream().map(SysConfigResponse::from).toList(),
                page.total(),
                normalizedPageNum,
                normalizedPageSize);
    }

    public SysConfigResponse getSysConfig(Long id) {
        requireAdmin();
        return SysConfigResponse.from(findSysConfig(id));
    }

    public Map<String, String> getEnabledConfigValues(List<String> keys) {
        requireAdmin();
        List<String> normalizedKeys = normalizeKeys(keys);
        if (normalizedKeys.isEmpty()) {
            return Map.of();
        }
        return repository.findEnabledConfigValues(normalizedKeys);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSysConfig(SysConfigUpdateRequest request) {
        requireAdmin();
        if (request == null || request.id() == null || request.id() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id must be positive");
        }
        findSysConfig(request.id());
        SysConfigUpdateCommand command = new SysConfigUpdateCommand(
                normalizeOptional(request.configValue()),
                normalizeOptional(request.description()),
                normalizeAvailableOrNull(request.isEnabled()));
        if (!repository.updateSysConfig(request.id(), command)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "sys config update failed");
        }
    }

    private ProfessionalItem findProfessional(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id must be positive");
        }
        return repository.findProfessionalById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "professional not found"));
    }

    private TradeLocationItem findTradeLocation(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id must be positive");
        }
        return repository.findTradeLocationById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "trade location not found"));
    }

    private SysConfigItem findSysConfig(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id must be positive");
        }
        return repository.findSysConfigById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "sys config not found"));
    }

    private ProfessionalCommand toProfessionalCommand(ProfessionalRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        String name = requireText(request.professionalName(), "professionalName", 100);
        String description = normalizeOptional(request.description());
        if (description != null && description.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "description length must be <= 500");
        }
        return new ProfessionalCommand(name, description);
    }

    private TradeLocationCommand toTradeLocationCommand(TradeLocationRequest request, boolean useDefaultAvailable) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request must not be null");
        }
        String name = requireText(request.locationName(), "locationName", 100);
        String description = normalizeOptional(request.locationDescription());
        String address = normalizeOptional(request.locationAddress());
        if (description != null && description.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "locationDescription length must be <= 500");
        }
        if (address != null && address.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "locationAddress length must be <= 200");
        }
        Integer available = normalizeAvailableOrNull(request.isAvailable());
        if (available == null && useDefaultAvailable) {
            available = 1;
        }
        if (available == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "isAvailable must not be null");
        }
        return new TradeLocationCommand(name, description, address, available);
    }

    private String requireText(String value, String field, int maxLength) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, field + " must not be blank");
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, field + " length must be <= " + maxLength);
        }
        return normalized;
    }

    private Integer normalizeAvailableOrNull(Integer value) {
        if (value == null) {
            return null;
        }
        if (value != 0 && value != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "value must be 0 or 1");
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

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "ids must not be empty");
        }
        return ids.stream()
                .map(id -> {
                    if (id == null || id <= 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "ids must be positive");
                    }
                    return id;
                })
                .distinct()
                .toList();
    }

    private List<String> normalizeKeys(List<String> keys) {
        if (keys == null) {
            return List.of();
        }
        Set<String> normalizedKeys = new LinkedHashSet<>();
        for (String key : keys) {
            String normalized = normalizeOptional(key);
            if (normalized != null) {
                normalizedKeys.add(normalized);
            }
        }
        return List.copyOf(normalizedKeys);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        return "1".equals(value.trim()) || "true".equalsIgnoreCase(value.trim());
    }

    private LoginUserContext requireAdmin() {
        LoginUserContext user = UserContextHolder.get()
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_LOGIN, "login required"));
        if (!Integer.valueOf(ADMIN_ROLE).equals(user.role())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "admin role required");
        }
        return user;
    }
}
