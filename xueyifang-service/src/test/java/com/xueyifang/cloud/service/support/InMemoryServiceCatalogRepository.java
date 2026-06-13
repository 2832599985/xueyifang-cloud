package com.xueyifang.cloud.service.support;

import com.xueyifang.cloud.service.repository.ServiceCatalogRepository;
import com.xueyifang.cloud.service.repository.ServiceCreateCommand;
import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.repository.ServiceListQuery;
import com.xueyifang.cloud.service.repository.ServicePage;
import com.xueyifang.cloud.service.repository.ServiceTag;
import com.xueyifang.cloud.service.repository.ServiceUpdateCommand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InMemoryServiceCatalogRepository implements ServiceCatalogRepository {

    private final Map<Long, ServiceItem> services = new LinkedHashMap<>();

    private final List<ServiceImage> images = new ArrayList<>();

    private final List<TagEntry> tags = new ArrayList<>();

    private long nextServiceId = 1L;

    private long nextImageId = 1L;

    public void putService(ServiceItem service) {
        services.put(service.id(), service);
        nextServiceId = Math.max(nextServiceId, service.id() + 1);
    }

    public void putImage(ServiceImage image) {
        images.add(image);
        nextImageId = Math.max(nextImageId, image.id() + 1);
    }

    public void putTag(Long id, String name, Integer sortOrder, Integer status) {
        tags.add(new TagEntry(id, name, sortOrder, status));
    }

    @Override
    public ServicePage findServices(ServiceListQuery query) {
        List<ServiceItem> matched = services.values().stream()
                .filter(service -> query.status() == null || query.status().equals(service.status()))
                .filter(service -> query.keyword() == null || containsIgnoreCase(service.title(), query.keyword())
                        || containsIgnoreCase(service.description(), query.keyword()))
                .filter(service -> query.tagId() == null || query.tagId().equals(service.tagId()))
                .filter(service -> query.categoryId() == null || query.categoryId().equals(service.categoryId()))
                .filter(service -> query.professionalId() == null
                        || query.professionalId().equals(service.professionalId()))
                .filter(service -> query.publisherId() == null || query.publisherId().equals(service.publisherId()))
                .sorted(Comparator.comparing(ServiceItem::updateTime).reversed()
                        .thenComparing(Comparator.comparing(ServiceItem::id).reversed()))
                .toList();

        List<ServiceItem> page = matched.stream()
                .skip(query.offset())
                .limit(query.limit())
                .toList();
        return new ServicePage(page, matched.size());
    }

    @Override
    public Optional<ServiceItem> findById(Long serviceId) {
        return Optional.ofNullable(services.get(serviceId));
    }

    @Override
    public Long createService(ServiceCreateCommand command) {
        long id = nextServiceId++;
        ServiceItem service = new ServiceItem(
                id,
                command.publisherId(),
                command.title(),
                command.description(),
                command.tagId(),
                command.tagName(),
                command.categoryId(),
                command.categoryName(),
                command.professionalId(),
                command.professionalName(),
                command.price(),
                command.unit(),
                command.location(),
                command.status(),
                command.reviewStatus(),
                0,
                0,
                BigDecimal.ZERO,
                command.coverImage(),
                java.time.LocalDateTime.parse("2026-06-14T00:00:00"),
                java.time.LocalDateTime.parse("2026-06-14T00:00:00"));
        services.put(id, service);
        return id;
    }

    @Override
    public boolean updateService(Long serviceId, ServiceUpdateCommand command) {
        ServiceItem service = services.get(serviceId);
        if (service == null) {
            return false;
        }

        services.put(serviceId, new ServiceItem(
                service.id(),
                service.publisherId(),
                valueOrCurrent(command.title(), service.title()),
                valueOrCurrent(command.description(), service.description()),
                valueOrCurrent(command.tagId(), service.tagId()),
                valueOrCurrent(command.tagName(), service.tagName()),
                valueOrCurrent(command.categoryId(), service.categoryId()),
                valueOrCurrent(command.categoryName(), service.categoryName()),
                valueOrCurrent(command.professionalId(), service.professionalId()),
                valueOrCurrent(command.professionalName(), service.professionalName()),
                valueOrCurrent(command.price(), service.price()),
                valueOrCurrent(command.unit(), service.unit()),
                valueOrCurrent(command.location(), service.location()),
                service.status(),
                service.reviewStatus(),
                service.favoriteCount(),
                service.orderCount(),
                service.rating(),
                valueOrCurrent(command.coverImage(), service.coverImage()),
                service.createTime(),
                java.time.LocalDateTime.parse("2026-06-14T00:00:00")));
        return true;
    }

    @Override
    public boolean updateServiceStatus(Long serviceId, int status, int reviewStatus) {
        ServiceItem service = services.get(serviceId);
        if (service == null) {
            return false;
        }

        services.put(serviceId, new ServiceItem(
                service.id(),
                service.publisherId(),
                service.title(),
                service.description(),
                service.tagId(),
                service.tagName(),
                service.categoryId(),
                service.categoryName(),
                service.professionalId(),
                service.professionalName(),
                service.price(),
                service.unit(),
                service.location(),
                status,
                reviewStatus,
                service.favoriteCount(),
                service.orderCount(),
                service.rating(),
                service.coverImage(),
                service.createTime(),
                java.time.LocalDateTime.parse("2026-06-14T00:00:00")));
        return true;
    }

    @Override
    public boolean updateCoverImage(Long serviceId, String coverImage) {
        ServiceItem service = services.get(serviceId);
        if (service == null) {
            return false;
        }

        services.put(serviceId, new ServiceItem(
                service.id(),
                service.publisherId(),
                service.title(),
                service.description(),
                service.tagId(),
                service.tagName(),
                service.categoryId(),
                service.categoryName(),
                service.professionalId(),
                service.professionalName(),
                service.price(),
                service.unit(),
                service.location(),
                service.status(),
                service.reviewStatus(),
                service.favoriteCount(),
                service.orderCount(),
                service.rating(),
                coverImage,
                service.createTime(),
                java.time.LocalDateTime.parse("2026-06-14T00:00:00")));
        return true;
    }

    @Override
    public boolean deleteService(Long serviceId) {
        return services.remove(serviceId) != null;
    }

    @Override
    public List<ServiceImage> findImagesByServiceId(Long serviceId) {
        return images.stream()
                .filter(image -> serviceId.equals(image.serviceId()))
                .sorted(Comparator.comparing(ServiceImage::cover).reversed()
                        .thenComparing(ServiceImage::sortOrder)
                        .thenComparing(ServiceImage::id))
                .toList();
    }

    @Override
    public void insertImages(Long serviceId, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(new ServiceImage(nextImageId++, serviceId, imageUrls.get(i), i, i == 0));
        }
    }

    @Override
    public void replaceImages(Long serviceId, List<String> imageUrls) {
        images.removeIf(image -> serviceId.equals(image.serviceId()));
        insertImages(serviceId, imageUrls);
    }

    @Override
    public List<ServiceTag> findActiveTags() {
        return tags.stream()
                .filter(tag -> Integer.valueOf(1).equals(tag.status()))
                .sorted(Comparator.comparing(TagEntry::sortOrder)
                        .thenComparing(TagEntry::id))
                .map(tag -> new ServiceTag(tag.id(), tag.name(), tag.sortOrder()))
                .toList();
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT)
                .contains(keyword.toLowerCase(Locale.ROOT));
    }

    private <T> T valueOrCurrent(T value, T current) {
        return value != null ? value : current;
    }

    private record TagEntry(Long id, String name, Integer sortOrder, Integer status) {
    }
}
