-- ============================================
-- 节点和订阅管理系统 - 数据库迁移脚本
-- Phase 4 & 5: 添加心跳和流量管理字段
-- ============================================

-- 1. 为 node 表添加 last_heartbeat_time 字段
ALTER TABLE `node` 
ADD COLUMN `last_heartbeat_time` DATETIME NULL COMMENT '最后心跳时间' AFTER `update_by`;

-- 2. 为 subscription 表添加流量相关字段
ALTER TABLE `subscription` 
ADD COLUMN `traffic_limit` BIGINT NULL COMMENT '流量限制(字节)' AFTER `valid_to`,
ADD COLUMN `traffic_used` BIGINT DEFAULT 0 COMMENT '已用流量(字节)' AFTER `traffic_limit`;

-- 3. 为相关字段添加索引以提升查询性能
ALTER TABLE `node` 
ADD INDEX `idx_last_heartbeat_time` (`last_heartbeat_time`);

ALTER TABLE `subscription` 
ADD INDEX `idx_valid_to` (`valid_to`),
ADD INDEX `idx_traffic` (`traffic_limit`, `traffic_used`);

-- 4. 验证表结构
SHOW COLUMNS FROM `node`;
SHOW COLUMNS FROM `subscription`;
