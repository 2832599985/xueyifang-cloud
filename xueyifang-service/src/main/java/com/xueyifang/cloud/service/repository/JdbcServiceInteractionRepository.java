package com.xueyifang.cloud.service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcServiceInteractionRepository implements ServiceInteractionRepository {

    private static final String SERVICE_COLUMNS = """
            s.id, s.publisher_id, s.title, s.description, s.tag_id, s.tag_name, s.category_id, s.category_name,
            s.professional_id, s.professional_name, s.price, s.unit, s.location, s.status, s.review_status,
            s.favorite_count, s.order_count, s.rating, s.cover_image, s.create_time, s.update_time
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcServiceInteractionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean addFavorite(Long userId, Long serviceId) {
        int updated = jdbcTemplate.update("""
                        INSERT IGNORE INTO service_favorite (user_id, service_id)
                        VALUES (?, ?)
                        """,
                userId,
                serviceId);
        return updated > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, Long serviceId) {
        int updated = jdbcTemplate.update("""
                        DELETE FROM service_favorite
                        WHERE user_id = ? AND service_id = ?
                        """,
                userId,
                serviceId);
        return updated > 0;
    }

    @Override
    public boolean existsFavorite(Long userId, Long serviceId) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_favorite
                        WHERE user_id = ? AND service_id = ?
                        """,
                Long.class,
                userId,
                serviceId);
        return count != null && count > 0;
    }

    @Override
    public FavoritePage findFavoritesByUser(Long userId, int offset, int limit) {
        Long total = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_favorite f
                        JOIN `service` s ON s.id = f.service_id AND s.is_deleted = 0
                        WHERE f.user_id = ?
                        """,
                Long.class,
                userId);

        List<FavoriteItem> records = jdbcTemplate.query("""
                        SELECT f.id AS favorite_id, f.create_time AS favorite_create_time,
                               COALESCE(NULLIF(u.real_name, ''), NULLIF(u.nickname, ''), u.username) AS seller_name,
                               %s
                        FROM service_favorite f
                        JOIN `service` s ON s.id = f.service_id AND s.is_deleted = 0
                        LEFT JOIN `user` u ON u.id = s.publisher_id AND u.is_deleted = 0
                        WHERE f.user_id = ?
                        ORDER BY f.create_time DESC, f.id DESC
                        LIMIT ? OFFSET ?
                        """.formatted(SERVICE_COLUMNS),
                (rs, rowNum) -> new FavoriteItem(
                        rs.getLong("favorite_id"),
                        mapService(rs),
                        rs.getString("seller_name"),
                        rs.getObject("favorite_create_time", java.time.LocalDateTime.class)),
                userId,
                limit,
                offset);

        return new FavoritePage(records, total != null ? total : 0L);
    }

    @Override
    public ServiceReviewPage findReviewsByService(Long serviceId, int offset, int limit) {
        Long total = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_review
                        WHERE service_id = ? AND is_deleted = 0
                        """,
                Long.class,
                serviceId);

        List<ServiceReviewItem> records = jdbcTemplate.query("""
                        SELECT r.id, r.service_id, r.order_id, r.buyer_id, r.seller_id, r.rating, r.content,
                               r.is_anonymous, r.create_time,
                               COALESCE(NULLIF(u.real_name, ''), NULLIF(u.nickname, ''), u.username) AS reviewer_name,
                               u.avatar AS reviewer_avatar
                        FROM service_review r
                        LEFT JOIN `user` u ON u.id = r.buyer_id AND u.is_deleted = 0
                        WHERE r.service_id = ? AND r.is_deleted = 0
                        ORDER BY r.create_time DESC, r.id DESC
                        LIMIT ? OFFSET ?
                        """,
                (rs, rowNum) -> new ServiceReviewItem(
                        rs.getLong("id"),
                        rs.getLong("service_id"),
                        rs.getLong("order_id"),
                        rs.getLong("buyer_id"),
                        rs.getLong("seller_id"),
                        rs.getObject("rating", Integer.class),
                        rs.getString("content"),
                        rs.getInt("is_anonymous") == 1,
                        rs.getObject("create_time", java.time.LocalDateTime.class),
                        rs.getString("reviewer_name"),
                        rs.getString("reviewer_avatar")),
                serviceId,
                limit,
                offset);

        return new ServiceReviewPage(records, total != null ? total : 0L);
    }

    @Override
    public boolean existsReviewByOrderId(Long orderId) {
        Long count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM service_review
                        WHERE order_id = ? AND is_deleted = 0
                        """,
                Long.class,
                orderId);
        return count != null && count > 0;
    }

    private ServiceItem mapService(ResultSet rs) throws SQLException {
        return new ServiceItem(
                rs.getLong("id"),
                rs.getObject("publisher_id", Long.class),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("tag_id", Long.class),
                rs.getString("tag_name"),
                rs.getObject("category_id", Long.class),
                rs.getString("category_name"),
                rs.getObject("professional_id", Long.class),
                rs.getString("professional_name"),
                rs.getBigDecimal("price"),
                rs.getString("unit"),
                rs.getString("location"),
                rs.getObject("status", Integer.class),
                rs.getObject("review_status", Integer.class),
                rs.getObject("favorite_count", Integer.class),
                rs.getObject("order_count", Integer.class),
                rs.getBigDecimal("rating"),
                rs.getString("cover_image"),
                rs.getObject("create_time", java.time.LocalDateTime.class),
                rs.getObject("update_time", java.time.LocalDateTime.class));
    }
}
