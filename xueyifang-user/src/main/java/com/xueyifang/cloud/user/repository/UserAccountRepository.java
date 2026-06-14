package com.xueyifang.cloud.user.repository;

import java.util.Set;
import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findById(Long id);

    boolean existsByEmailExcludingUser(String email, Long userId);

    boolean existsByPhoneExcludingUser(String phone, Long userId);

    boolean updateProfile(Long userId, UserProfileUpdateCommand command);

    boolean updatePassword(Long userId, String password);

    boolean updatePermissionApplication(Long userId, int publishPermission, int permissionReviewStatus,
                                        String applyReason);

    UserAccountPage findPendingPermissionUsers(int offset, int limit);

    boolean updatePermissionReview(Long userId, int publishPermission, int permissionReviewStatus,
                                   String reviewReason, Long reviewedBy);

    Set<String> findExistingStudentIds();

    Set<String> findExistingUsernames();

    Set<Long> findActiveProfessionalIds();

    Long createImportedUser(UserImportCreateCommand command);
}
