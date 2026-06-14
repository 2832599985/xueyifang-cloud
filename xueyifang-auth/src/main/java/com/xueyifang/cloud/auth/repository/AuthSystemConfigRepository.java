package com.xueyifang.cloud.auth.repository;

import java.util.Optional;

public interface AuthSystemConfigRepository {

    Optional<String> findEnabledConfigValue(String key);
}
