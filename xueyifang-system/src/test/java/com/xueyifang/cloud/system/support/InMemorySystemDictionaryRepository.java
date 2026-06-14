package com.xueyifang.cloud.system.support;

import com.xueyifang.cloud.system.repository.ProfessionalCommand;
import com.xueyifang.cloud.system.repository.ProfessionalItem;
import com.xueyifang.cloud.system.repository.SysConfigItem;
import com.xueyifang.cloud.system.repository.SysConfigUpdateCommand;
import com.xueyifang.cloud.system.repository.SystemDictionaryRepository;
import com.xueyifang.cloud.system.repository.SystemPage;
import com.xueyifang.cloud.system.repository.TradeLocationCommand;
import com.xueyifang.cloud.system.repository.TradeLocationItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class InMemorySystemDictionaryRepository implements SystemDictionaryRepository {

    private final Map<Long, ProfessionalEntry> professionals = new LinkedHashMap<>();

    private final Map<Long, TradeLocationEntry> tradeLocations = new LinkedHashMap<>();

    private final Map<Long, SysConfigItem> sysConfigs = new LinkedHashMap<>();

    private final AtomicLong professionalId = new AtomicLong(1);

    private final AtomicLong tradeLocationId = new AtomicLong(1);

    private final AtomicLong sysConfigId = new AtomicLong(1);

    public void clear() {
        professionals.clear();
        tradeLocations.clear();
        sysConfigs.clear();
        professionalId.set(1);
        tradeLocationId.set(1);
        sysConfigId.set(1);
    }

    public Long putProfessional(String name, String description, boolean deleted) {
        Long id = professionalId.getAndIncrement();
        LocalDateTime now = now(id);
        professionals.put(id, new ProfessionalEntry(
                new ProfessionalItem(id, name, description, now, now),
                deleted));
        return id;
    }

    public Long putTradeLocation(String name, String description, String address, int available, boolean deleted) {
        Long id = tradeLocationId.getAndIncrement();
        LocalDateTime now = now(id);
        tradeLocations.put(id, new TradeLocationEntry(
                new TradeLocationItem(id, name, description, address, available, now, now),
                deleted));
        return id;
    }

    public Long putSysConfig(String key, String value, String description, int enabled) {
        Long id = sysConfigId.getAndIncrement();
        LocalDateTime now = now(id);
        sysConfigs.put(id, new SysConfigItem(id, key, value, description, enabled, now, now));
        return id;
    }

    @Override
    public List<ProfessionalItem> findProfessionals() {
        return professionals.values().stream()
                .filter(entry -> !entry.deleted())
                .map(ProfessionalEntry::item)
                .sorted(Comparator.comparing(ProfessionalItem::createTime).reversed())
                .toList();
    }

    @Override
    public Optional<ProfessionalItem> findProfessionalById(Long id) {
        ProfessionalEntry entry = professionals.get(id);
        if (entry == null || entry.deleted()) {
            return Optional.empty();
        }
        return Optional.of(entry.item());
    }

    @Override
    public SystemPage<ProfessionalItem> findProfessionals(String nameLike, int offset, int limit) {
        List<ProfessionalItem> matched = findProfessionals().stream()
                .filter(item -> contains(item.professionalName(), nameLike))
                .toList();
        return new SystemPage<>(page(matched, offset, limit), matched.size());
    }

    @Override
    public long countProfessionalsByName(String professionalName, Long excludeId) {
        return professionals.values().stream()
                .filter(entry -> !entry.deleted())
                .map(ProfessionalEntry::item)
                .filter(item -> !item.id().equals(excludeId))
                .filter(item -> item.professionalName().equals(professionalName))
                .count();
    }

    @Override
    public Long createProfessional(ProfessionalCommand command) {
        return putProfessional(command.professionalName(), command.description(), false);
    }

    @Override
    public boolean updateProfessional(Long id, ProfessionalCommand command) {
        ProfessionalEntry entry = professionals.get(id);
        if (entry == null || entry.deleted()) {
            return false;
        }
        ProfessionalItem previous = entry.item();
        professionals.put(id, new ProfessionalEntry(new ProfessionalItem(
                id,
                command.professionalName(),
                command.description(),
                previous.createTime(),
                previous.updateTime().plusSeconds(1)), false));
        return true;
    }

    @Override
    public boolean deleteProfessional(Long id) {
        ProfessionalEntry entry = professionals.get(id);
        if (entry == null || entry.deleted()) {
            return false;
        }
        professionals.put(id, new ProfessionalEntry(entry.item(), true));
        return true;
    }

    @Override
    public List<TradeLocationItem> findTradeLocations(boolean availableOnly) {
        Predicate<TradeLocationItem> availableFilter = availableOnly
                ? item -> Integer.valueOf(1).equals(item.isAvailable())
                : item -> true;
        return tradeLocations.values().stream()
                .filter(entry -> !entry.deleted())
                .map(TradeLocationEntry::item)
                .filter(availableFilter)
                .sorted(Comparator.comparing(TradeLocationItem::createTime).reversed())
                .toList();
    }

    @Override
    public Optional<TradeLocationItem> findTradeLocationById(Long id) {
        TradeLocationEntry entry = tradeLocations.get(id);
        if (entry == null || entry.deleted()) {
            return Optional.empty();
        }
        return Optional.of(entry.item());
    }

    @Override
    public SystemPage<TradeLocationItem> findTradeLocations(Integer isAvailable, String nameLike, int offset,
                                                           int limit) {
        List<TradeLocationItem> matched = findTradeLocations(false).stream()
                .filter(item -> isAvailable == null || isAvailable.equals(item.isAvailable()))
                .filter(item -> contains(item.locationName(), nameLike))
                .toList();
        return new SystemPage<>(page(matched, offset, limit), matched.size());
    }

    @Override
    public long countTradeLocationsByName(String locationName, Long excludeId) {
        return tradeLocations.values().stream()
                .filter(entry -> !entry.deleted())
                .map(TradeLocationEntry::item)
                .filter(item -> !item.id().equals(excludeId))
                .filter(item -> item.locationName().equals(locationName))
                .count();
    }

    @Override
    public Long createTradeLocation(TradeLocationCommand command) {
        return putTradeLocation(
                command.locationName(),
                command.locationDescription(),
                command.locationAddress(),
                command.isAvailable(),
                false);
    }

    @Override
    public boolean updateTradeLocation(Long id, TradeLocationCommand command) {
        TradeLocationEntry entry = tradeLocations.get(id);
        if (entry == null || entry.deleted()) {
            return false;
        }
        TradeLocationItem previous = entry.item();
        tradeLocations.put(id, new TradeLocationEntry(new TradeLocationItem(
                id,
                command.locationName(),
                command.locationDescription(),
                command.locationAddress(),
                command.isAvailable(),
                previous.createTime(),
                previous.updateTime().plusSeconds(1)), false));
        return true;
    }

    @Override
    public boolean deleteTradeLocation(Long id) {
        TradeLocationEntry entry = tradeLocations.get(id);
        if (entry == null || entry.deleted()) {
            return false;
        }
        tradeLocations.put(id, new TradeLocationEntry(entry.item(), true));
        return true;
    }

    @Override
    public int deleteTradeLocations(List<Long> ids) {
        int deleted = 0;
        for (Long id : ids) {
            if (deleteTradeLocation(id)) {
                deleted++;
            }
        }
        return deleted;
    }

    @Override
    public Optional<SysConfigItem> findEnabledConfigByKey(String key) {
        return sysConfigs.values().stream()
                .filter(config -> Integer.valueOf(1).equals(config.isEnabled()))
                .filter(config -> config.configKey().equals(key))
                .findFirst();
    }

    @Override
    public Optional<SysConfigItem> findSysConfigById(Long id) {
        return Optional.ofNullable(sysConfigs.get(id));
    }

    @Override
    public SystemPage<SysConfigItem> findSysConfigs(String keyLike, int offset, int limit) {
        List<SysConfigItem> matched = sysConfigs.values().stream()
                .filter(config -> contains(config.configKey(), keyLike))
                .sorted(Comparator.comparing(SysConfigItem::id))
                .toList();
        return new SystemPage<>(page(matched, offset, limit), matched.size());
    }

    @Override
    public Map<String, String> findEnabledConfigValues(List<String> keys) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : keys) {
            findEnabledConfigByKey(key).ifPresent(config ->
                    values.put(config.configKey(), config.configValue() != null ? config.configValue() : ""));
        }
        return values;
    }

    @Override
    public boolean updateSysConfig(Long id, SysConfigUpdateCommand command) {
        SysConfigItem previous = sysConfigs.get(id);
        if (previous == null) {
            return false;
        }
        sysConfigs.put(id, new SysConfigItem(
                id,
                previous.configKey(),
                command.configValue() != null ? command.configValue() : previous.configValue(),
                command.description() != null ? command.description() : previous.description(),
                command.isEnabled() != null ? command.isEnabled() : previous.isEnabled(),
                previous.createTime(),
                previous.updateTime().plusSeconds(1)));
        return true;
    }

    private <T> List<T> page(List<T> items, int offset, int limit) {
        if (offset >= items.size()) {
            return List.of();
        }
        return new ArrayList<>(items.subList(offset, Math.min(offset + limit, items.size())));
    }

    private boolean contains(String value, String keyword) {
        if (keyword == null) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private LocalDateTime now(Long id) {
        return LocalDateTime.parse("2026-06-14T00:00:00").plusSeconds(id);
    }

    private record ProfessionalEntry(ProfessionalItem item, boolean deleted) {
    }

    private record TradeLocationEntry(TradeLocationItem item, boolean deleted) {
    }
}
