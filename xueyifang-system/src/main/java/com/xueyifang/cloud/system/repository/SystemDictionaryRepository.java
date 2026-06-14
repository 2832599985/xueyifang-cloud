package com.xueyifang.cloud.system.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SystemDictionaryRepository {

    List<ProfessionalItem> findProfessionals();

    Optional<ProfessionalItem> findProfessionalById(Long id);

    SystemPage<ProfessionalItem> findProfessionals(String nameLike, int offset, int limit);

    long countProfessionalsByName(String professionalName, Long excludeId);

    Long createProfessional(ProfessionalCommand command);

    boolean updateProfessional(Long id, ProfessionalCommand command);

    boolean deleteProfessional(Long id);

    List<TradeLocationItem> findTradeLocations(boolean availableOnly);

    Optional<TradeLocationItem> findTradeLocationById(Long id);

    SystemPage<TradeLocationItem> findTradeLocations(Integer isAvailable, String nameLike, int offset, int limit);

    long countTradeLocationsByName(String locationName, Long excludeId);

    Long createTradeLocation(TradeLocationCommand command);

    boolean updateTradeLocation(Long id, TradeLocationCommand command);

    boolean deleteTradeLocation(Long id);

    int deleteTradeLocations(List<Long> ids);

    Optional<SysConfigItem> findEnabledConfigByKey(String key);

    Optional<SysConfigItem> findSysConfigById(Long id);

    SystemPage<SysConfigItem> findSysConfigs(String keyLike, int offset, int limit);

    Map<String, String> findEnabledConfigValues(List<String> keys);

    boolean updateSysConfig(Long id, SysConfigUpdateCommand command);
}
