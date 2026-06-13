package com.xueyifang.cloud.service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    public Long createService(ServiceCreateCommand command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO `service`
                                (publisher_id, title, description, tag_id, tag_name, category_id, category_name,
                                 professional_id, professional_name, price, unit, location, status, review_status,
                                 cover_image)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, command.publisherId());
            statement.setString(2, command.title());
            statement.setString(3, command.description());
            setNullableLong(statement, 4, command.tagId());
            statement.setString(5, command.tagName());
            setNullableLong(statement, 6, command.categoryId());
            statement.setString(7, command.categoryName());
            setNullableLong(statement, 8, command.professionalId());
            statement.setString(9, command.professionalName());
            statement.setBigDecimal(10, command.price());
            statement.setString(11, command.unit());
            statement.setString(12, command.location());
            statement.setInt(13, command.status());
            statement.setInt(14, command.reviewStatus());
            statement.setString(15, command.coverImage());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("service id was not generated");
        }
        return key.longValue();
    }

    @Override
    public boolean updateService(Long serviceId, ServiceUpdateCommand command) {
        if (!command.hasChanges()) {
            return true;
        }

        List<String> assignments = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        addAssignment(assignments, parameters, "title", command.title());
        addAssignment(assignments, parameters, "description", command.description());
        addAssignment(assignments, parameters, "tag_id", command.tagId());
        addAssignment(assignments, parameters, "tag_name", command.tagName());
        addAssignment(assignments, parameters, "category_id", command.categoryId());
        addAssignment(assignments, parameters, "category_name", command.categoryName());
        addAssignment(assignments, parameters, "professional_id", command.professionalId());
        addAssignment(assignments, parameters, "professional_name", command.professionalName());
        addAssignment(assignments, parameters, "price", command.price());
        addAssignment(assignments, parameters, "unit", command.unit());
        addAssignment(assignments, parameters, "location", command.location());
        addAssignment(assignments, parameters, "cover_image", command.coverImage());

        parameters.add(serviceId);
        int updated = jdbcTemplate.update(
                "UPDATE `service` SET " + String.join(", ", assignments)
                        + " WHERE id = ? AND is_deleted = 0",
                parameters.toArray());
        return updated > 0;
    }

    @Override
    public boolean updateServiceStatus(Long serviceId, int status, int reviewStatus) {
        int updated = jdbcTemplate.update("""
                        UPDATE `service`
                        SET status = ?, review_status = ?
                        WHERE id = ? AND is_deleted = 0
                        """,
                status,
                reviewStatus,
                serviceId);
        return updated > 0;
    }

    @Override
    public boolean updateCoverImage(Long serviceId, String coverImage) {
        int updated = jdbcTemplate.update("""
                        UPDATE `service`
                        SET cover_image = ?
                        WHERE id = ? AND is_deleted = 0
                        """,
                coverImage,
                serviceId);
        return updated > 0;
    }

    @Override
    public boolean deleteService(Long serviceId) {
        int updated = jdbcTemplate.update(
                "UPDATE `service` SET is_deleted = 1 WHERE id = ? AND is_deleted = 0",
                serviceId);
        return updated > 0;
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
    public void insertImages(Long serviceId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            jdbcTemplate.update("""
                            INSERT INTO service_image (service_id, image_url, sort_order, is_cover)
                            VALUES (?, ?, ?, ?)
                            """,
                    serviceId,
                    imageUrls.get(i),
                    i,
                    i == 0);
        }
    }

    @Override
    public void replaceImages(Long serviceId, List<String> imageUrls) {
        jdbcTemplate.update("""
                        UPDATE service_image
                        SET is_deleted = 1
                        WHERE service_id = ? AND is_deleted = 0
                        """,
                serviceId);
        insertImages(serviceId, imageUrls);
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
        if (query.publisherId() != null) {
            where.append(" AND publisher_id = ?");
            parameters.add(query.publisherId());
        }

        return where.toString();
    }

    private void addAssignment(List<String> assignments, List<Object> parameters, String column, Object value) {
        if (value != null) {
            assignments.add(column + " = ?");
            parameters.add(value);
        }
    }

    private void setNullableLong(PreparedStatement statement, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            statement.setObject(parameterIndex, null);
            return;
        }
        statement.setLong(parameterIndex, value);
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
