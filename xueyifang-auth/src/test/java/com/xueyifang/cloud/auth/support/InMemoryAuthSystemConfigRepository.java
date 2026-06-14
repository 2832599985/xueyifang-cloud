package com.xueyifang.cloud.auth.support;

import com.xueyifang.cloud.auth.repository.AuthSystemConfigRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryAuthSystemConfigRepository implements AuthSystemConfigRepository {

    private final Map<String, String> values = new LinkedHashMap<>();

    @Override
    public Optional<String> findEnabledConfigValue(String key) {
        return Optional.ofNullable(values.get(key));
    }

    public void put(String key, String value) {
        values.put(key, value);
    }
}
