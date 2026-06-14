package com.xueyifang.cloud.user.support;

import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.repository.UserAccountPage;
import com.xueyifang.cloud.user.repository.UserAccountRepository;
import com.xueyifang.cloud.user.repository.UserImportCreateCommand;
import com.xueyifang.cloud.user.repository.UserProfileUpdateCommand;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryUserAccountRepository implements UserAccountRepository {

    private final Map<Long, UserAccount> users = new HashMap<>();

    private final Set<Long> professionalIds = new HashSet<>();

    public void put(UserAccount user) {
        users.put(user.id(), user);
    }

    public void putProfessionalId(Long professionalId) {
        professionalIds.add(professionalId);
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean existsByEmailExcludingUser(String email, Long userId) {
        return users.values().stream()
                .anyMatch(user -> !user.id().equals(userId) && email.equalsIgnoreCase(user.email()));
    }

    @Override
    public boolean existsByPhoneExcludingUser(String phone, Long userId) {
        return users.values().stream()
                .anyMatch(user -> !user.id().equals(userId) && phone.equals(user.phone()));
    }

    @Override
    public boolean updateProfile(Long userId, UserProfileUpdateCommand command) {
        UserAccount user = users.get(userId);
        if (user == null) {
            return false;
        }

        users.put(userId, new UserAccount(
                user.id(),
                user.username(),
                user.password(),
                user.studentId(),
                valueOrExisting(command.realName(), user.realName()),
                valueOrExisting(command.nickname(), user.nickname()),
                valueOrExisting(command.phone(), user.phone()),
                valueOrExisting(command.email(), user.email()),
                valueOrExisting(command.dormitory(), user.dormitory()),
                valueOrExisting(command.grade(), user.grade()),
                command.professionalId() != null ? command.professionalId() : user.professionalId(),
                valueOrExisting(command.avatar(), user.avatar()),
                valueOrExisting(command.bio(), user.bio()),
                user.role(),
                user.publishPermission(),
                user.permissionReviewStatus(),
                user.permissionApplyReason(),
                user.permissionReviewReason(),
                user.permissionReviewedBy(),
                user.permissionReviewedAt(),
                user.walletBalance(),
                user.frozenAmount(),
                user.status(),
                user.accountStatus(),
                user.createTime(),
                user.updateTime()));
        return true;
    }

    @Override
    public boolean updatePassword(Long userId, String password) {
        UserAccount user = users.get(userId);
        if (user == null) {
            return false;
        }

        users.put(userId, new UserAccount(
                user.id(),
                user.username(),
                password,
                user.studentId(),
                user.realName(),
                user.nickname(),
                user.phone(),
                user.email(),
                user.dormitory(),
                user.grade(),
                user.professionalId(),
                user.avatar(),
                user.bio(),
                user.role(),
                user.publishPermission(),
                user.permissionReviewStatus(),
                user.permissionApplyReason(),
                user.permissionReviewReason(),
                user.permissionReviewedBy(),
                user.permissionReviewedAt(),
                user.walletBalance(),
                user.frozenAmount(),
                user.status(),
                user.accountStatus(),
                user.createTime(),
                user.updateTime()));
        return true;
    }

    @Override
    public boolean updatePermissionApplication(Long userId, int publishPermission, int permissionReviewStatus,
                                               String applyReason) {
        UserAccount user = users.get(userId);
        if (user == null) {
            return false;
        }

        users.put(userId, new UserAccount(
                user.id(),
                user.username(),
                user.password(),
                user.studentId(),
                user.realName(),
                user.nickname(),
                user.phone(),
                user.email(),
                user.dormitory(),
                user.grade(),
                user.professionalId(),
                user.avatar(),
                user.bio(),
                user.role(),
                publishPermission,
                permissionReviewStatus,
                applyReason,
                null,
                null,
                null,
                user.walletBalance(),
                user.frozenAmount(),
                user.status(),
                user.accountStatus(),
                user.createTime(),
                LocalDateTime.parse("2026-06-14T00:00:00")));
        return true;
    }

    @Override
    public UserAccountPage findPendingPermissionUsers(int offset, int limit) {
        var matched = users.values().stream()
                .filter(user -> Integer.valueOf(0).equals(user.permissionReviewStatus()))
                .sorted(Comparator.comparing(UserAccount::updateTime)
                        .thenComparing(UserAccount::id))
                .toList();
        var records = matched.stream()
                .skip(offset)
                .limit(limit)
                .toList();
        return new UserAccountPage(records, matched.size());
    }

    @Override
    public boolean updatePermissionReview(Long userId, int publishPermission, int permissionReviewStatus,
                                          String reviewReason, Long reviewedBy) {
        UserAccount user = users.get(userId);
        if (user == null || !Integer.valueOf(0).equals(user.permissionReviewStatus())) {
            return false;
        }

        users.put(userId, new UserAccount(
                user.id(),
                user.username(),
                user.password(),
                user.studentId(),
                user.realName(),
                user.nickname(),
                user.phone(),
                user.email(),
                user.dormitory(),
                user.grade(),
                user.professionalId(),
                user.avatar(),
                user.bio(),
                user.role(),
                publishPermission,
                permissionReviewStatus,
                user.permissionApplyReason(),
                reviewReason,
                reviewedBy,
                LocalDateTime.parse("2026-06-14T00:00:00"),
                user.walletBalance(),
                user.frozenAmount(),
                user.status(),
                user.accountStatus(),
                user.createTime(),
                user.updateTime()));
        return true;
    }

    @Override
    public Set<String> findExistingStudentIds() {
        return users.values().stream()
                .map(UserAccount::studentId)
                .filter(studentId -> studentId != null && !studentId.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> findExistingUsernames() {
        return users.values().stream()
                .map(UserAccount::username)
                .filter(username -> username != null && !username.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> findActiveProfessionalIds() {
        return Set.copyOf(professionalIds);
    }

    @Override
    public Long createImportedUser(UserImportCreateCommand command) {
        Long id = users.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        LocalDateTime now = LocalDateTime.parse("2026-06-14T00:00:00").plusSeconds(id);
        users.put(id, new UserAccount(
                id,
                command.username(),
                command.password(),
                command.studentId(),
                command.realName(),
                command.nickname(),
                command.phone(),
                command.email(),
                command.dormitory(),
                command.grade(),
                command.professionalId(),
                null,
                null,
                command.role(),
                command.publishPermission(),
                command.permissionReviewStatus(),
                null,
                null,
                null,
                null,
                valueOrZero(command.walletBalance()),
                valueOrZero(command.frozenAmount()),
                command.status(),
                command.accountStatus(),
                now,
                now));
        return id;
    }

    private String valueOrExisting(String value, String existing) {
        return value != null ? value : existing;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
