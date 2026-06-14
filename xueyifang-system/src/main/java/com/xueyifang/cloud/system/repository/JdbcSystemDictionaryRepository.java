package com.xueyifang.cloud.system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcSystemDictionaryRepository implements SystemDictionaryRepository {

    private static final String PROFESSIONAL_COLUMNS =
            "id, professional_name, description, create_time, update_time";

    private static final String TRADE_LOCATION_COLUMNS =
            "id, location_name, location_description, location_address, is_available, create_time, update_time";

    private static final String SYS_CONFIG_COLUMNS =
            "id, config_key, config_value, description, is_enabled, create_time, update_time";

    private final JdbcTemplate jdbcTemplate;

    public JdbcSystemDictionaryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ProfessionalItem> findProfessionals() {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM professional
                        WHERE is_delete = 0
                        ORDER BY create_time DESC, id DESC
                        """.formatted(PROFESSIONAL_COLUMNS),
                (rs, rowNum) -> mapProfessional(rs));
    }

    @Override
    public Optional<ProfessionalItem> findProfessionalById(Long id) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM professional
                        WHERE id = ? AND is_delete = 0
                        LIMIT 1
                        """.formatted(PROFESSIONAL_COLUMNS),
                ps -> ps.setLong(1, id),
                rs -> rs.next() ? Optional.of(mapProfessional(rs)) : Optional.empty());
    }

    @Override
    public SystemPage<ProfessionalItem> findProfessionals(String nameLike, int offset, int limit) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildProfessionalWhereClause(nameLike, parameters);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM professional " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(limit);
        listParameters.add(offset);
        List<ProfessionalItem> records = jdbcTemplate.query(
                "SELECT " + PROFESSIONAL_COLUMNS + " FROM professional " + whereClause
                        + " ORDER BY create_time DESC, id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapProfessional(rs),
                listParameters.toArray());
        return new SystemPage<>(records, total != null ? total : 0L);
    }

    @Override
    public long countProfessionalsByName(String professionalName, Long excludeId) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(professionalName);
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(1) FROM professional WHERE professional_name = ? AND is_delete = 0");
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            parameters.add(excludeId);
        }
        Long total = jdbcTemplate.queryForObject(sql.toString(), Long.class, parameters.toArray());
        return total != null ? total : 0L;
    }

    @Override
    public Long createProfessional(ProfessionalCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO professional (professional_name, description)
                            VALUES (?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, command.professionalName());
            statement.setString(2, command.description());
            return statement;
        }, keyHolder);
        return generatedId(keyHolder, "professional id was not generated");
    }

    @Override
    public boolean updateProfessional(Long id, ProfessionalCommand command) {
        int updated = jdbcTemplate.update("""
                        UPDATE professional
                        SET professional_name = ?, description = ?
                        WHERE id = ? AND is_delete = 0
                        """,
                command.professionalName(),
                command.description(),
                id);
        return updated > 0;
    }

    @Override
    public boolean deleteProfessional(Long id) {
        int updated = jdbcTemplate.update(
                "UPDATE professional SET is_delete = 1 WHERE id = ? AND is_delete = 0",
                id);
        return updated > 0;
    }

    @Override
    public List<TradeLocationItem> findTradeLocations(boolean availableOnly) {
        List<Object> parameters = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE is_delete = 0");
        if (availableOnly) {
            where.append(" AND is_available = ?");
            parameters.add(1);
        }
        return jdbcTemplate.query(
                "SELECT " + TRADE_LOCATION_COLUMNS + " FROM trade_location " + where
                        + " ORDER BY create_time DESC, id DESC",
                (rs, rowNum) -> mapTradeLocation(rs),
                parameters.toArray());
    }

    @Override
    public Optional<TradeLocationItem> findTradeLocationById(Long id) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM trade_location
                        WHERE id = ? AND is_delete = 0
                        LIMIT 1
                        """.formatted(TRADE_LOCATION_COLUMNS),
                ps -> ps.setLong(1, id),
                rs -> rs.next() ? Optional.of(mapTradeLocation(rs)) : Optional.empty());
    }

    @Override
    public SystemPage<TradeLocationItem> findTradeLocations(Integer isAvailable, String nameLike,
                                                            int offset, int limit) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildTradeLocationWhereClause(isAvailable, nameLike, parameters);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM trade_location " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(limit);
        listParameters.add(offset);
        List<TradeLocationItem> records = jdbcTemplate.query(
                "SELECT " + TRADE_LOCATION_COLUMNS + " FROM trade_location " + whereClause
                        + " ORDER BY create_time DESC, id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapTradeLocation(rs),
                listParameters.toArray());
        return new SystemPage<>(records, total != null ? total : 0L);
    }

    @Override
    public long countTradeLocationsByName(String locationName, Long excludeId) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(locationName);
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(1) FROM trade_location WHERE location_name = ? AND is_delete = 0");
        if (excludeId != null) {
            sql.append(" AND id <> ?");
            parameters.add(excludeId);
        }
        Long total = jdbcTemplate.queryForObject(sql.toString(), Long.class, parameters.toArray());
        return total != null ? total : 0L;
    }

    @Override
    public Long createTradeLocation(TradeLocationCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO trade_location
                                (location_name, location_description, location_address, is_available)
                            VALUES (?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, command.locationName());
            statement.setString(2, command.locationDescription());
            statement.setString(3, command.locationAddress());
            statement.setInt(4, command.isAvailable());
            return statement;
        }, keyHolder);
        return generatedId(keyHolder, "trade location id was not generated");
    }

    @Override
    public boolean updateTradeLocation(Long id, TradeLocationCommand command) {
        int updated = jdbcTemplate.update("""
                        UPDATE trade_location
                        SET location_name = ?, location_description = ?, location_address = ?, is_available = ?
                        WHERE id = ? AND is_delete = 0
                        """,
                command.locationName(),
                command.locationDescription(),
                command.locationAddress(),
                command.isAvailable(),
                id);
        return updated > 0;
    }

    @Override
    public boolean deleteTradeLocation(Long id) {
        int updated = jdbcTemplate.update(
                "UPDATE trade_location SET is_delete = 1 WHERE id = ? AND is_delete = 0",
                id);
        return updated > 0;
    }

    @Override
    public int deleteTradeLocations(List<Long> ids) {
        if (ids.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(", ", Collections.nCopies(ids.size(), "?"));
        return jdbcTemplate.update(
                "UPDATE trade_location SET is_delete = 1 WHERE is_delete = 0 AND id IN (" + placeholders + ")",
                ids.toArray());
    }

    @Override
    public Optional<SysConfigItem> findEnabledConfigByKey(String key) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM sys_config
                        WHERE config_key = ? AND is_enabled = 1
                        LIMIT 1
                        """.formatted(SYS_CONFIG_COLUMNS),
                ps -> ps.setString(1, key),
                rs -> rs.next() ? Optional.of(mapSysConfig(rs)) : Optional.empty());
    }

    @Override
    public Optional<SysConfigItem> findSysConfigById(Long id) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM sys_config
                        WHERE id = ?
                        LIMIT 1
                        """.formatted(SYS_CONFIG_COLUMNS),
                ps -> ps.setLong(1, id),
                rs -> rs.next() ? Optional.of(mapSysConfig(rs)) : Optional.empty());
    }

    @Override
    public SystemPage<SysConfigItem> findSysConfigs(String keyLike, int offset, int limit) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildSysConfigWhereClause(keyLike, parameters);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_config " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(limit);
        listParameters.add(offset);
        List<SysConfigItem> records = jdbcTemplate.query(
                "SELECT " + SYS_CONFIG_COLUMNS + " FROM sys_config " + whereClause
                        + " ORDER BY id ASC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapSysConfig(rs),
                listParameters.toArray());
        return new SystemPage<>(records, total != null ? total : 0L);
    }

    @Override
    public Map<String, String> findEnabledConfigValues(List<String> keys) {
        if (keys.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(", ", Collections.nCopies(keys.size(), "?"));
        List<SysConfigItem> configs = jdbcTemplate.query(
                "SELECT " + SYS_CONFIG_COLUMNS + " FROM sys_config"
                        + " WHERE is_enabled = 1 AND config_key IN (" + placeholders + ")",
                (rs, rowNum) -> mapSysConfig(rs),
                keys.toArray());

        Map<String, String> values = new LinkedHashMap<>();
        for (SysConfigItem config : configs) {
            values.put(config.configKey(), config.configValue() != null ? config.configValue() : "");
        }
        return values;
    }

    @Override
    public boolean updateSysConfig(Long id, SysConfigUpdateCommand command) {
        if (!command.hasChanges()) {
            return true;
        }
        List<String> assignments = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        addAssignment(assignments, parameters, "config_value", command.configValue());
        addAssignment(assignments, parameters, "description", command.description());
        addAssignment(assignments, parameters, "is_enabled", command.isEnabled());
        parameters.add(id);

        int updated = jdbcTemplate.update(
                "UPDATE sys_config SET " + String.join(", ", assignments) + " WHERE id = ?",
                parameters.toArray());
        return updated > 0;
    }

    private String buildProfessionalWhereClause(String nameLike, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE is_delete = 0");
        if (nameLike != null) {
            where.append(" AND professional_name LIKE ?");
            parameters.add("%" + nameLike + "%");
        }
        return where.toString();
    }

    private String buildTradeLocationWhereClause(Integer isAvailable, String nameLike, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE is_delete = 0");
        if (isAvailable != null) {
            where.append(" AND is_available = ?");
            parameters.add(isAvailable);
        }
        if (nameLike != null) {
            where.append(" AND location_name LIKE ?");
            parameters.add("%" + nameLike + "%");
        }
        return where.toString();
    }

    private String buildSysConfigWhereClause(String keyLike, List<Object> parameters) {
        if (keyLike == null) {
            return "";
        }
        parameters.add("%" + keyLike + "%");
        return "WHERE config_key LIKE ?";
    }

    private void addAssignment(List<String> assignments, List<Object> parameters, String column, Object value) {
        if (value != null) {
            assignments.add(column + " = ?");
            parameters.add(value);
        }
    }

    private Long generatedId(KeyHolder keyHolder, String errorMessage) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException(errorMessage);
        }
        return key.longValue();
    }

    private ProfessionalItem mapProfessional(ResultSet rs) throws SQLException {
        return new ProfessionalItem(
                rs.getLong("id"),
                rs.getString("professional_name"),
                rs.getString("description"),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }

    private TradeLocationItem mapTradeLocation(ResultSet rs) throws SQLException {
        return new TradeLocationItem(
                rs.getLong("id"),
                rs.getString("location_name"),
                rs.getString("location_description"),
                rs.getString("location_address"),
                rs.getObject("is_available", Integer.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }

    private SysConfigItem mapSysConfig(ResultSet rs) throws SQLException {
        return new SysConfigItem(
                rs.getLong("id"),
                rs.getString("config_key"),
                rs.getString("config_value"),
                rs.getString("description"),
                rs.getObject("is_enabled", Integer.class),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }
}
