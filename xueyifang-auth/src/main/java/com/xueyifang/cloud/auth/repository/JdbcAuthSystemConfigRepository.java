package com.xueyifang.cloud.auth.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcAuthSystemConfigRepository implements AuthSystemConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthSystemConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> findEnabledConfigValue(String key) {
        return jdbcTemplate.query("""
                        SELECT config_value
                        FROM sys_config
                        WHERE config_key = ? AND is_enabled = 1
                        LIMIT 1
                        """,
                ps -> ps.setString(1, key),
                rs -> rs.next() ? Optional.ofNullable(rs.getString("config_value")) : Optional.empty());
    }
}
