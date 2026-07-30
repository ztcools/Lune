CREATE DATABASE IF NOT EXISTS lune DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lune;

DROP TABLE IF EXISTS `diary`;
DROP TABLE IF EXISTS `visit_log`;
DROP TABLE IF EXISTS `resource`;
DROP TABLE IF EXISTS `site_config`;
DROP TABLE IF EXISTS `family`;
DROP TABLE IF EXISTS `record`;
DROP TABLE IF EXISTS `essay`;
DROP TABLE IF EXISTS `tree_hole`;
DROP TABLE IF EXISTS `article_tag`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `work_experience`;
DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `wish`;
DROP TABLE IF EXISTS `wish_like`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `gender` VARCHAR(10) DEFAULT '保密',
    `birthday` DATE DEFAULT '2000-01-01',
    `signature` VARCHAR(200) DEFAULT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200) DEFAULT NULL,
    `type` VARCHAR(20) NOT NULL DEFAULT 'article',
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `color` VARCHAR(20) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` LONGTEXT,
    `summary` VARCHAR(500) DEFAULT NULL,
    `cover` VARCHAR(500) DEFAULT NULL,
    `view_count` BIGINT NOT NULL DEFAULT 0,
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `is_top` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `article_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT DEFAULT NULL,
    `type` VARCHAR(20) DEFAULT NULL COMMENT '评论类型(article/essay/love/family/treehole)',
    `source_id` BIGINT DEFAULT NULL COMMENT '来源ID',
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `reply_to` BIGINT DEFAULT NULL,
    `content` TEXT NOT NULL,
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_type_source` (`type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tree_hole` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `content` TEXT NOT NULL,
    `color` VARCHAR(20) DEFAULT NULL,
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `essay` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `content` TEXT NOT NULL,
    `cover` VARCHAR(500) DEFAULT NULL,
    `media` TEXT DEFAULT NULL COMMENT 'JSON媒体列表 [{type:image|video, url}]',
    `weather` VARCHAR(20) DEFAULT NULL,
    `mood` VARCHAR(20) DEFAULT NULL,
    `location` VARCHAR(100) DEFAULT NULL,
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `diary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `content` TEXT,
    `images` TEXT DEFAULT NULL,
    `record_time` DATETIME DEFAULT NULL,
    `page_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT,
    `cover` VARCHAR(500) DEFAULT NULL,
    `media` TEXT DEFAULT NULL COMMENT 'JSON媒体列表 [{type:image|video, url}]',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `family` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) DEFAULT NULL,
    `content` TEXT,
    `cover` VARCHAR(500) DEFAULT NULL,
    `bg_cover` VARCHAR(500) DEFAULT NULL,
    `man_cover` VARCHAR(500) DEFAULT NULL,
    `woman_cover` VARCHAR(500) DEFAULT NULL,
    `man_name` VARCHAR(50) DEFAULT NULL,
    `woman_name` VARCHAR(50) DEFAULT NULL,
    `timing` VARCHAR(100) DEFAULT NULL,
    `countdown_title` VARCHAR(200) DEFAULT NULL,
    `countdown_time` DATETIME DEFAULT NULL,
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `site_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL,
    `config_value` TEXT,
    `config_type` VARCHAR(20) NOT NULL DEFAULT 'public',
    `description` VARCHAR(200) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `filename` VARCHAR(255) NOT NULL,
    `path` VARCHAR(500) NOT NULL,
    `size` BIGINT NOT NULL DEFAULT 0,
    `mime_type` VARCHAR(100) DEFAULT NULL,
    `type` VARCHAR(20) DEFAULT 'image',
    `store_type` VARCHAR(20) NOT NULL DEFAULT 'local',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 访问日志：地理字段由 GeoIpService 解析后写入（province 已归一化为
-- 中国地图 GeoJSON 的全称，如「浙江省」，可直接 GROUP BY 喂给地图）
CREATE TABLE `visit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ip` VARCHAR(50) DEFAULT NULL,
    `country` VARCHAR(50) DEFAULT NULL,
    `province` VARCHAR(50) DEFAULT NULL,
    `city` VARCHAR(50) DEFAULT NULL,
    `longitude` DECIMAL(10,6) DEFAULT NULL,
    `latitude` DECIMAL(10,6) DEFAULT NULL,
    `user_agent` VARCHAR(500) DEFAULT NULL,
    `path` VARCHAR(200) DEFAULT NULL,
    `method` VARCHAR(10) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ip` (`ip`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_province` (`province`),
    KEY `idx_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `work_experience` (
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
    `media` TEXT DEFAULT NULL COMMENT 'JSON媒体 [{type:image|video, url}]',
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(100) NOT NULL,
    `summary` VARCHAR(300) DEFAULT NULL,
    `description` TEXT,
    `tech_stack` TEXT DEFAULT NULL COMMENT 'JSON数组 ["Vue3","SpringBoot"]',
    `role` VARCHAR(100) DEFAULT NULL,
    `project_url` VARCHAR(500) DEFAULT NULL,
    `repo_url` VARCHAR(500) DEFAULT NULL,
    `cover` VARCHAR(500) DEFAULT NULL,
    `media` TEXT DEFAULT NULL COMMENT 'JSON媒体 [{type:image|video, url}]',
    `dev_period` VARCHAR(50) DEFAULT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `wish` (
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

CREATE TABLE `wish_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `wish_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wish_user` (`wish_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2b$10$WgpN2.yz6/7unb1Vx4VzpetLjwb3q6rjl9hE6R3O9C4rh/juJVD2.', 'Lune', 'ADMIN', 1);

INSERT INTO `category` (`name`, `description`, `type`, `sort_order`) VALUES
('技术', '技术相关文章', 'article', 1),
('生活', '生活随笔', 'article', 2),
('学习', '学习笔记', 'record', 1),
('游戏', '游戏记录', 'record', 2),
('旅游', '旅游记录', 'record', 3),
('美食', '美食记录', 'record', 4),
('音乐', '音乐收藏', 'record', 5);

INSERT INTO `site_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('site_name', 'Lune', 'public', '网站名称'),
('site_title', 'Lune - 记录美好生活', 'public', '网站标题'),
('site_description', '个人博客，记录成长，分享生活', 'public', '网站描述'),
('site_logo', '', 'public', '网站Logo'),
('site_footer', '© 2024 Lune. All Rights Reserved.', 'public', '页脚信息'),
('notices', '["欢迎来到 Lune！"]', 'public', '网站公告'),
('about_content', '', 'public', '关于页面内容'),
('enable_register', 'true', 'public', '是否开放注册'),
('enable_comment', 'true', 'public', '是否开放评论'),
('landing_bg', '[]', 'public', 'Landing页背景图'),
('home_hero_bg', '[]', 'public', '首页顶部背景图'),
('home_content_bg', '[]', 'public', '首页内容区背景图'),
('family_hero_bg', '[]', 'public', '家页顶部背景图'),
('family_content_bg', '[]', 'public', '家页内容区背景图'),
('treehole_danmaku_bg', '[]', 'public', '树洞弹幕背景图'),
('treehole_content_bg', '[]', 'public', '树洞时间线背景图'),
('essay_hero_bg', '[]', 'public', '随笔页顶部背景图'),
('essay_content_bg', '[]', 'public', '随笔页内容区背景图'),
('record_hero_bg', '[]', 'public', '记录页顶部背景图'),
('record_content_bg', '[]', 'public', '记录页内容区背景图');
