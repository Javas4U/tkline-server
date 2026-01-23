-- ============================================
-- 节点管理系统 - 添加 domain 字段并迁移数据
-- 创建时间: 2026-01-21
-- 说明:
-- 1. 新增 domain 字段用于存储域名
-- 2. 将现有 ip_address 字段的值迁移到 domain 字段
-- 3. ip_address 字段保留但不再是必填项
-- ============================================

-- 添加 domain 字段
ALTER TABLE `node`
ADD COLUMN `domain` VARCHAR(255) NULL COMMENT '域名' AFTER `name`;

-- 迁移现有数据：将 ip_address 的值复制到 domain
UPDATE `node`
SET `domain` = `ip_address`
WHERE `domain` IS NULL;

-- 验证表结构
SHOW COLUMNS FROM `node`;
