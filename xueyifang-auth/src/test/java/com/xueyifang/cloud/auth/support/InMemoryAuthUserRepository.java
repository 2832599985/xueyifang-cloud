package com.xueyifang.cloud.auth.support;

import com.xueyifang.cloud.auth.repository.AuthUser;
import com.xueyifang.cloud.auth.repository.AuthUserCreateCommand;
import com.xueyifang.cloud.auth.repository.AuthUserRepository;
import org.springframework.dao.DuplicateKeyException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAuthUserRepository implements AuthUserRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final Map<String, AuthUser> usersByUsername = new LinkedHashMap<>();

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username);
    }

    @Override
    public AuthUser create(AuthUserCreateCommand command) {
        if (usersByUsername.containsKey(command.username())) {
            throw new DuplicateKeyException("duplicate username");
        }

        AuthUser user = new AuthUser(
                idGenerator.getAndIncrement(),
                command.username(),
                command.password(),
                command.nickname(),
                command.role(),
                command.publishPermission(),
                command.status());
        usersByUsername.put(user.username(), user);
        return user;
    }

    public void put(AuthUser user) {
        usersByUsername.put(user.username(), user);
    }
}
