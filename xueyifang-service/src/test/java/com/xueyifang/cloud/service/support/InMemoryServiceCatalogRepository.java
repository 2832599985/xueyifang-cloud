package com.xueyifang.cloud.service.support;

import com.xueyifang.cloud.service.repository.ServiceCatalogRepository;
import com.xueyifang.cloud.service.repository.ServiceImage;
import com.xueyifang.cloud.service.repository.ServiceItem;
import com.xueyifang.cloud.service.repository.ServiceListQuery;
import com.xueyifang.cloud.service.repository.ServicePage;
import com.xueyifang.cloud.service.repository.ServiceTag;

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

    public void putService(ServiceItem service) {
        services.put(service.id(), service);
    }

    public void putImage(ServiceImage image) {
        images.add(image);
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
    public List<ServiceImage> findImagesByServiceId(Long serviceId) {
        return images.stream()
                .filter(image -> serviceId.equals(image.serviceId()))
                .sorted(Comparator.comparing(ServiceImage::cover).reversed()
                        .thenComparing(ServiceImage::sortOrder)
                        .thenComparing(ServiceImage::id))
                .toList();
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

    private record TagEntry(Long id, String name, Integer sortOrder, Integer status) {
    }
}
