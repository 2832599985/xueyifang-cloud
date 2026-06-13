package com.xueyifang.cloud.auth.service;

import java.util.Arrays;

enum AuthUserRole {

    STUDENT("STUDENT", 1, 1),
    ADMIN("ADMIN", 2, 1);

    private final String databaseValue;

    private final int tokenCode;

    private final int publishPermission;

    AuthUserRole(String databaseValue, int tokenCode, int publishPermission) {
        this.databaseValue = databaseValue;
        this.tokenCode = tokenCode;
        this.publishPermission = publishPermission;
    }

    String databaseValue() {
        return databaseValue;
    }

    int tokenCode() {
        return tokenCode;
    }

    int publishPermission() {
        return publishPermission;
    }

    static AuthUserRole fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.databaseValue.equalsIgnoreCase(value))
                .findFirst()
                .orElse(STUDENT);
    }
}
