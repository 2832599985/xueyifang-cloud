package com.xueyifang.cloud.auth.repository;

import java.util.Optional;

public interface AuthUserRepository {

    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    AuthUser create(AuthUserCreateCommand command);
}
