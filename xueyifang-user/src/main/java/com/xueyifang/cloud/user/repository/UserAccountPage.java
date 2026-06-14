package com.xueyifang.cloud.user.repository;

import java.util.List;

public record UserAccountPage(List<UserAccount> records, long total) {
}
