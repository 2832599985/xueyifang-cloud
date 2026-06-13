package com.xueyifang.cloud.service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcServiceCatalogRepository implements ServiceCatalogRepository {

    private static final String SERVICE_COLUMNS = """
            id, publisher_id, title, description, tag_id, tag_name, category_id, category_name,
            professional_id, professional_name, price, unit, location, status, review_status,
            favorite_count, order_count, rating, cover_image, create_time, update_time
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcServiceCatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ServicePage findServices(ServiceListQuery query) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(query, parameters);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM `service` " + whereClause,
                Long.class,
                parameters.toArray());

        List<Object> listParameters = new ArrayList<>(parameters);
        listParameters.add(query.limit());
        listParameters.add(query.offset());

        List<ServiceItem> records = jdbcTemplate.query(
                "SELECT " + SERVICE_COLUMNS + " FROM `service` " + whereClause
                        + " ORDER BY update_time DESC, id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> mapService(rs),
                listParameters.toArray());

        return new ServicePage(records, total != null ? total : 0L);
    }

    @Override
    public Optional<ServiceItem> findById(Long serviceId) {
        return jdbcTemplate.query(
                "SELECT " + SERVICE_COLUMNS + " FROM `service` WHERE id = ? AND is_deleted = 0 LIMIT 1",
                ps -> ps.setLong(1, serviceId),
                rs -> rs.next() ? Optional.of(mapService(rs)) : Optional.empty());
    }

    @Override
    public List<ServiceImage> findImagesByServiceId(Long serviceId) {
        return jdbcTemplate.query("""
                        SELECT id, service_id, image_url, sort_order, is_cover
                        FROM service_image
                        WHERE service_id = ? AND is_deleted = 0
                        ORDER BY is_cover DESC, sort_order ASC, id ASC
                        """,
                (rs, rowNum) -> new ServiceImage(
                        rs.getLong("id"),
                        rs.getLong("service_id"),
                        rs.getString("image_url"),
                        rs.getObject("sort_order", Integer.class),
                        rs.getBoolean("is_cover")),
                serviceId);
    }

    @Override
    public List<ServiceTag> findActiveTags() {
        return jdbcTemplate.query("""
                        SELECT id, name, sort_order
                        FROM service_tag
                        WHERE status = 1 AND is_deleted = 0
                        ORDER BY sort_order ASC, id ASC
                        """,
                (rs, rowNum) -> new ServiceTag(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getObject("sort_order", Integer.class)));
    }

    private String buildWhereClause(ServiceListQuery query, List<Object> parameters) {
        StringBuilder where = new StringBuilder("WHERE is_deleted = 0");

        if (query.status() != null) {
            where.append(" AND status = ?");
            parameters.add(query.status());
        }
        if (query.keyword() != null) {
            where.append(" AND (title LIKE ? OR description LIKE ?)");
            String keyword = "%" + query.keyword() + "%";
            parameters.add(keyword);
            parameters.add(keyword);
        }
        if (query.tagId() != null) {
            where.append(" AND tag_id = ?");
            parameters.add(query.tagId());
        }
        if (query.categoryId() != null) {
            where.append(" AND category_id = ?");
            parameters.add(query.categoryId());
        }
        if (query.professionalId() != null) {
            where.append(" AND professional_id = ?");
            parameters.add(query.professionalId());
        }

        return where.toString();
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
