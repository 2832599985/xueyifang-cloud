package com.xueyifang.cloud.service.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcServiceReviewModeRepository implements ServiceReviewModeRepository {

    private static final String REVIEW_MODE_KEY = "REVIEW_MODE";

    private static final String NO_REVIEW_MODE = "2";

    private final JdbcTemplate jdbcTemplate;

    public JdbcServiceReviewModeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean requiresReview() {
        try {
            String value = jdbcTemplate.queryForObject("""
                            SELECT config_value
                            FROM sys_config
                            WHERE config_key = ? AND is_enabled = 1
                            LIMIT 1
                            """,
                    String.class,
                    REVIEW_MODE_KEY);
            return value == null || !NO_REVIEW_MODE.equals(value.trim());
        } catch (EmptyResultDataAccessException exception) {
            return true;
        }
    }
}
