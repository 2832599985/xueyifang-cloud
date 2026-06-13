package com.xueyifang.cloud.common.core.auth;

import java.util.Optional;

public final class AuthTokenUtils {

    private AuthTokenUtils() {
    }

    public static Optional<String> resolveToken(String authorizationHeader, String legacyTokenHeader) {
        Optional<String> bearerToken = resolveBearerToken(authorizationHeader);
        if (bearerToken.isPresent()) {
            return bearerToken;
        }

        if (legacyTokenHeader == null || legacyTokenHeader.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(legacyTokenHeader.trim());
    }

    public static Optional<String> resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }

        String trimmedHeader = authorizationHeader.trim();
        if (!trimmedHeader.regionMatches(true, 0, AuthConstants.BEARER_PREFIX, 0,
                AuthConstants.BEARER_PREFIX.length())) {
            return Optional.empty();
        }

        String token = trimmedHeader.substring(AuthConstants.BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(token);
    }
}
