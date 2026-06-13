package com.xueyifang.cloud.user.service;

import java.util.Arrays;

public enum UserRole {

    STUDENT("STUDENT", 1),
    ADMIN("ADMIN", 2);

    private final String databaseValue;

    private final int code;

    UserRole(String databaseValue, int code) {
        this.databaseValue = databaseValue;
        this.code = code;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public int code() {
        return code;
    }

    public static UserRole fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.databaseValue.equalsIgnoreCase(value))
                .findFirst()
                .orElse(STUDENT);
    }
}
