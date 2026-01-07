-- ============================================
-- 节点和订阅管理系统 - 数据库迁移脚本
-- Phase: 添加节点上行下行配额字段
-- ============================================

-- 为 node 表添加上行下行配额字段
ALTER TABLE `node` 
ADD COLUMN `upstream_quota` INT NULL COMMENT '上行配额(Mbps)' AFTER `description`,
ADD COLUMN `downstream_quota` INT NULL COMMENT '下行配额(Mbps)' AFTER `upstream_quota`;

-- 为配额字段添加索引以提升查询性能
ALTER TABLE `node` 
ADD INDEX `idx_quota` (`upstream_quota`, `downstream_quota`);

-- 验证表结构
SHOW COLUMNS FROM `node`;
