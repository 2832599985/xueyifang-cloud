CREATE TABLE IF NOT EXISTS `professional` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `professional_name` VARCHAR(100) NOT NULL COMMENT 'professional name',
    `description` VARCHAR(500) DEFAULT NULL COMMENT 'professional description',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_professional_name` (`professional_name`),
    KEY `idx_professional_delete_time` (`is_delete`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='professional dictionary';

CREATE TABLE IF NOT EXISTS `trade_location` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `location_name` VARCHAR(100) NOT NULL COMMENT 'location name',
    `location_description` VARCHAR(500) DEFAULT NULL COMMENT 'location description',
    `location_address` VARCHAR(200) DEFAULT NULL COMMENT 'location address',
    `is_available` TINYINT NOT NULL DEFAULT 1 COMMENT '0 unavailable, 1 available',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'logical delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_location_name` (`location_name`),
    KEY `idx_trade_location_available_time` (`is_delete`, `is_available`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='trade location dictionary';

CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `config_key` VARCHAR(100) NOT NULL COMMENT 'config key',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT 'config value',
    `description` VARCHAR(500) DEFAULT NULL COMMENT 'config description',
    `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'created time',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
        COMMENT 'updated time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_config_key` (`config_key`),
    KEY `idx_sys_config_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='system config';

INSERT INTO `professional` (`professional_name`, `description`) VALUES
    ('计算机科学与技术', '计算机科学与技术相关专业'),
    ('软件工程', '软件开发与设计相关专业'),
    ('信息与计算科学', '信息处理相关专业'),
    ('电子信息工程', '电子与信息工程相关专业'),
    ('数据科学与大数据技术', '大数据处理与分析'),
    ('艺术设计', '视觉设计与艺术设计'),
    ('工商管理', '商业与管理相关专业'),
    ('英语', '英语语言与文化'),
    ('其他', '其他专业')
,
    ('SERVICE_TITLE_MAX_LENGTH', '255', '服务标题最大长度', 1),
    ('SERVICE_DESC_MAX_LENGTH', '2000', '服务描述最大长度', 1),
    ('FROZEN_RELEASE_PERCENTAGE', '100', '自动释放时释放给卖家的冻结资金百分比（0-100）', 1)
ON DUPLICATE KEY UPDATE
    `description` = VALUES(`description`);

INSERT INTO `trade_location` (`location_name`, `location_description`, `location_address`) VALUES
    ('校园咖啡厅', '学生常聚集场所，环境舒适', '校园中心广场旁'),
    ('图书馆一楼大厅', '人多，安全可靠', '图书馆入口'),
    ('宿舍楼下', '方便快捷，就近交易', '各宿舍楼下'),
    ('食堂门口', '人流量大，交易便捷', '学生食堂门口'),
    ('运动场旁', '开放空间，视野开阔', '学校运动场南侧')
,
    ('SERVICE_TITLE_MAX_LENGTH', '255', '服务标题最大长度', 1),
    ('SERVICE_DESC_MAX_LENGTH', '2000', '服务描述最大长度', 1),
    ('FROZEN_RELEASE_PERCENTAGE', '100', '自动释放时释放给卖家的冻结资金百分比（0-100）', 1)
ON DUPLICATE KEY UPDATE
    `location_description` = VALUES(`location_description`),
    `location_address` = VALUES(`location_address`),
    `is_available` = 1,
    `is_delete` = 0;

INSERT INTO `sys_config` (`config_key`, `config_value`, `description`, `is_enabled`) VALUES
    ('REGISTER_ENABLED', '1', 'self-service registration switch, 1 enabled and 0 disabled', 1),
    ('REVIEW_MODE', '1', 'service review mode', 1),
    ('ORDER_UNPAID_TIMEOUT_HOURS', '24', 'unpaid order auto cancel timeout in hours', 1),
    ('SELLER_REFUND_TIMEOUT_DAYS', '3', 'seller refund handling timeout in days', 1),
    ('AUTO_CONFIRM_RECEIPT_DAYS', '7', 'auto confirm receipt timeout in days', 1)
,
    ('SERVICE_TITLE_MAX_LENGTH', '255', '服务标题最大长度', 1),
    ('SERVICE_DESC_MAX_LENGTH', '2000', '服务描述最大长度', 1),
    ('FROZEN_RELEASE_PERCENTAGE', '100', '自动释放时释放给卖家的冻结资金百分比（0-100）', 1)
ON DUPLICATE KEY UPDATE
    `config_value` = VALUES(`config_value`),
    `description` = VALUES(`description`),
    `is_enabled` = VALUES(`is_enabled`);
