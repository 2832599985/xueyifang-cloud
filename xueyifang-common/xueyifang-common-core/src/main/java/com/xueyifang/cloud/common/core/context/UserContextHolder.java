package com.xueyifang.cloud.common.core.context;

import java.util.Optional;

public final class UserContextHolder {

    private static final ThreadLocal<LoginUserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(LoginUserContext context) {
        HOLDER.set(context);
    }

    public static Optional<LoginUserContext> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static Optional<Long> currentUserId() {
        return get().map(LoginUserContext::userId);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
