CREATE TABLE IF NOT EXISTS `service_tag` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `name` VARCHAR(64) NOT NULL COMMENT 'tag name',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 active',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_tag_name` (`name`),
    KEY `idx_service_tag_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='service tag';

CREATE TABLE IF NOT EXISTS `service` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `publisher_id` BIGINT UNSIGNED NOT NULL COMMENT 'publisher user id',
    `title` VARCHAR(100) NOT NULL COMMENT 'service title',
    `description` TEXT DEFAULT NULL COMMENT 'service description',
    `tag_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'tag id',
    `tag_name` VARCHAR(64) DEFAULT NULL COMMENT 'tag name snapshot',
    `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'category id',
    `category_name` VARCHAR(64) DEFAULT NULL COMMENT 'category name snapshot',
    `professional_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'professional id',
    `professional_name` VARCHAR(100) DEFAULT NULL COMMENT 'professional name snapshot',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'service price',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT 'price unit',
    `location` VARCHAR(100) DEFAULT NULL COMMENT 'service location',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0 offline, 1 online, 2 reviewing, 3 rejected',
    `review_status` TINYINT NOT NULL DEFAULT 1 COMMENT '0 pending, 1 approved, 2 rejected',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT 'favorite count',
    `order_count` INT NOT NULL DEFAULT 0 COMMENT 'order count',
    `rating` DECIMAL(3, 2) NOT NULL DEFAULT 0.00 COMMENT 'average rating',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT 'cover image url',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_service_publisher` (`publisher_id`),
    KEY `idx_service_status_update_time` (`status`, `update_time`),
    KEY `idx_service_tag` (`tag_id`),
    KEY `idx_service_category` (`category_id`),
    KEY `idx_service_professional` (`professional_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='service';

CREATE TABLE IF NOT EXISTS `service_image` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `service_id` BIGINT UNSIGNED NOT NULL COMMENT 'service id',
    `image_url` VARCHAR(255) NOT NULL COMMENT 'image url',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
    `is_cover` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'cover image flag',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_service_image_service` (`service_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='service image';

CREATE TABLE IF NOT EXISTS `service_favorite` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'user id',
    `service_id` BIGINT UNSIGNED NOT NULL COMMENT 'service id',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_favorite_user_service` (`user_id`, `service_id`),
    KEY `idx_service_favorite_service` (`service_id`),
    KEY `idx_service_favorite_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='service favorite';

CREATE TABLE IF NOT EXISTS `service_review` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `service_id` BIGINT UNSIGNED NOT NULL COMMENT 'service id',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT 'order id',
    `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT 'buyer user id',
    `seller_id` BIGINT UNSIGNED NOT NULL COMMENT 'seller user id',
    `rating` TINYINT NOT NULL COMMENT 'rating 1-5',
    `content` VARCHAR(500) NOT NULL COMMENT 'review content',
    `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'anonymous flag',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_review_order` (`order_id`),
    KEY `idx_service_review_service` (`service_id`, `create_time`),
    KEY `idx_service_review_buyer` (`buyer_id`),
    KEY `idx_service_review_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='service review';
