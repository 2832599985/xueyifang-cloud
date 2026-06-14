CREATE TABLE IF NOT EXISTS `user_chat` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `sender_id` BIGINT UNSIGNED NOT NULL COMMENT 'sender user id',
    `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT 'receiver user id',
    `content` TEXT NOT NULL COMMENT 'chat content',
    `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1 text, 2 image, 3 file',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '0 unread, 1 read',
    `related_service_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'related service id',
    `related_order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'related order id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_user_chat_sender_receiver` (`sender_id`, `receiver_id`, `create_time`),
    KEY `idx_user_chat_receiver_read` (`receiver_id`, `is_read`, `create_time`),
    KEY `idx_user_chat_related_service` (`related_service_id`),
    KEY `idx_user_chat_related_order` (`related_order_id`),
    CONSTRAINT `fk_user_chat_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_user_chat_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user chat';

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `recipient_id` BIGINT UNSIGNED NOT NULL COMMENT 'recipient user id',
    `notification_type` TINYINT NOT NULL COMMENT '1 permission, 2 removal, 3 order, 4 dispute, 5 service review',
    `title` VARCHAR(200) NOT NULL COMMENT 'notification title',
    `content` TEXT NOT NULL COMMENT 'notification content',
    `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'related business id',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '0 unread, 1 read',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`),
    KEY `idx_notification_recipient_read` (`recipient_id`, `is_read`, `create_time`),
    KEY `idx_notification_recipient_type` (`recipient_id`, `notification_type`, `create_time`),
    KEY `idx_notification_create_time` (`create_time`),
    CONSTRAINT `fk_notification_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='notification';
