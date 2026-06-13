package com.xueyifang.cloud.user.repository;

import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findById(Long id);

    boolean existsByEmailExcludingUser(String email, Long userId);

    boolean existsByPhoneExcludingUser(String phone, Long userId);

    boolean updateProfile(Long userId, UserProfileUpdateCommand command);

    boolean updatePassword(Long userId, String password);

    boolean updatePermissionApplication(Long userId, int publishPermission, int permissionReviewStatus);
}
