CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `username` VARCHAR(32) NOT NULL COMMENT 'username',
    `password` VARCHAR(128) NOT NULL COMMENT 'encrypted password',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT 'nickname',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'avatar url',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'phone',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'email',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT 'STUDENT/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 active',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_phone` (`phone`),
    KEY `idx_user_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user';
