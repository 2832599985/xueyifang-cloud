package com.xueyifang.cloud.user.support;

import com.xueyifang.cloud.user.repository.UserAccount;
import com.xueyifang.cloud.user.repository.UserAccountRepository;
import com.xueyifang.cloud.user.repository.UserProfileUpdateCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserAccountRepository implements UserAccountRepository {

    private final Map<Long, UserAccount> users = new HashMap<>();

    public void put(UserAccount user) {
        users.put(user.id(), user);
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
                user.walletBalance(),
                user.frozenAmount(),
                user.status(),
                user.accountStatus(),
                user.createTime(),
                user.updateTime()));
        return true;
    }

    @Override
    public boolean updatePermissionApplication(Long userId, int publishPermission, int permissionReviewStatus) {
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
                user.walletBalance(),
                user.frozenAmount(),
                user.status(),
                user.accountStatus(),
                user.createTime(),
                user.updateTime()));
        return true;
    }

    private String valueOrExisting(String value, String existing) {
        return value != null ? value : existing;
    }
}
