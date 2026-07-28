-- ============================================================
-- Lune 增量迁移：新增 简历(work_experience/project) + 许愿池(wish/wish_like)
-- 以及 essay.media 字段。对现有数据库安全（IF NOT EXISTS）。
-- 执行方式：docker exec -i lune-mysql-dev mysql -uroot -p<PWD> lune < migration-20260728.sql
-- ============================================================

-- essay 增加 media 字段（若不存在）
SET @col := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'essay' AND COLUMN_NAME = 'media'
);
SET @ddl := IF(@col = 0,
  'ALTER TABLE `essay` ADD COLUMN `media` TEXT DEFAULT NULL COMMENT ''JSON媒体 [{type:image|video, url}]'' AFTER `cover`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `work_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `company` VARCHAR(100) NOT NULL,
    `position` VARCHAR(100) DEFAULT NULL,
    `location` VARCHAR(100) DEFAULT NULL,
    `start_date` DATE DEFAULT NULL,
    `end_date` DATE DEFAULT NULL,
    `is_current` TINYINT(1) NOT NULL DEFAULT 0,
    `description` TEXT,
    `responsibilities` TEXT,
    `media` TEXT DEFAULT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(100) NOT NULL,
    `summary` VARCHAR(300) DEFAULT NULL,
    `description` TEXT,
    `tech_stack` TEXT DEFAULT NULL,
    `role` VARCHAR(100) DEFAULT NULL,
    `project_url` VARCHAR(500) DEFAULT NULL,
    `repo_url` VARCHAR(500) DEFAULT NULL,
    `cover` VARCHAR(500) DEFAULT NULL,
    `media` TEXT DEFAULT NULL,
    `dev_period` VARCHAR(50) DEFAULT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `wish` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_like_count` (`like_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `wish_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `wish_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wish_user` (`wish_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 许愿池 / 简历页背景 + 简历个人卡片元信息配置（幂等）
INSERT IGNORE INTO `site_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('wish_hero_bg', '[]', 'public', '许愿池顶部背景图'),
('wish_content_bg', '[]', 'public', '许愿池内容区背景图'),
('resume_hero_bg', '[]', 'public', '简历页顶部背景图'),
('resume_skills', 'Vue / Spring Boot / 全栈开发', 'public', '简历-擅长技术栈'),
('resume_hobbies', '编程 / 摄影 / 旅行 / 音乐', 'public', '简历-爱好'),
('resume_github', 'https://github.com/ztcools', 'public', '简历-GitHub地址'),
('resume_motto', '时刻保持思考，永远热爱生活', 'public', '简历-座右铭'),
('resume_tags', '["全栈开发","热爱开源","持续学习","生活记录者"]', 'public', '简历-个人标签');
