-- ============================================================
-- Migration 2026-07-30: 访问统计系统升级
-- 1. visit_log 表新增地理位置字段（国家/省/市/经纬度）
-- 2. 新增 method 字段（GET/POST）
-- 3. 新增索引（IP、创建时间、省份、路径）用于快速统计
--
-- 幂等：deploy.sh 每次部署都会重跑所有 migration-*.sql，
-- 而 MySQL 不支持 ADD COLUMN IF NOT EXISTS，因此这里查
-- information_schema 后再决定是否执行 ALTER。
-- 新建库不需要本文件（sql/lune.sql 与 docker/mysql/init/01-init.sql
-- 已含完整定义），它只用于升级已存在的旧表。
-- ============================================================

USE lune;

DROP PROCEDURE IF EXISTS lune_migrate_visit_log;
DELIMITER $$
CREATE PROCEDURE lune_migrate_visit_log()
BEGIN
    -- 列
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'visit_log'
                     AND COLUMN_NAME = 'country') THEN
        ALTER TABLE `visit_log`
            ADD COLUMN `country`   VARCHAR(50)    NULL DEFAULT NULL AFTER `ip`,
            ADD COLUMN `province`  VARCHAR(50)    NULL DEFAULT NULL AFTER `country`,
            ADD COLUMN `city`      VARCHAR(50)    NULL DEFAULT NULL AFTER `province`,
            ADD COLUMN `longitude` DECIMAL(10,6)  NULL DEFAULT NULL AFTER `city`,
            ADD COLUMN `latitude`  DECIMAL(10,6)  NULL DEFAULT NULL AFTER `longitude`;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'visit_log'
                     AND COLUMN_NAME = 'method') THEN
        ALTER TABLE `visit_log` ADD COLUMN `method` VARCHAR(10) NULL DEFAULT NULL AFTER `path`;
    END IF;

    -- 索引（逐个判断：老库可能已手工加过一部分）
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'visit_log'
                     AND INDEX_NAME = 'idx_ip') THEN
        ALTER TABLE `visit_log` ADD INDEX `idx_ip` (`ip`);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'visit_log'
                     AND INDEX_NAME = 'idx_create_time') THEN
        ALTER TABLE `visit_log` ADD INDEX `idx_create_time` (`create_time`);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'visit_log'
                     AND INDEX_NAME = 'idx_province') THEN
        ALTER TABLE `visit_log` ADD INDEX `idx_province` (`province`);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'visit_log'
                     AND INDEX_NAME = 'idx_path') THEN
        ALTER TABLE `visit_log` ADD INDEX `idx_path` (`path`);
    END IF;
END$$
DELIMITER ;

CALL lune_migrate_visit_log();
DROP PROCEDURE IF EXISTS lune_migrate_visit_log;
