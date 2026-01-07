-- ============================================================================
-- 节点与订阅管理系统 - 数据库建表脚本
-- 功能分支: 014-node-subscription
-- 创建时间: 2026-01-07
-- ============================================================================

-- 1. 创建节点表
CREATE TABLE `node` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `ip_address` VARCHAR(45) NOT NULL COMMENT 'IP地址',
  `port` INT NOT NULL COMMENT '端口号',
  `region` VARCHAR(50) DEFAULT NULL COMMENT '地区',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0=禁用,1=启用',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '节点描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) NOT NULL COMMENT '创建人用户名',
  `update_by` VARCHAR(50) NOT NULL COMMENT '更新人用户名',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0=未删除,1=已删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_name` (`name`),
  KEY `idx_node_status` (`status`),
  KEY `idx_node_region` (`region`),
  KEY `idx_node_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节点表';

-- 2. 创建订阅表
CREATE TABLE `subscription` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '订阅名称',
  `type` VARCHAR(50) NOT NULL COMMENT '订阅类型',
  `valid_from` DATETIME NOT NULL COMMENT '有效期开始时间',
  `valid_to` DATETIME NOT NULL COMMENT '有效期结束时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0=禁用,1=有效,2=过期',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '订阅描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) NOT NULL COMMENT '创建人用户名',
  `update_by` VARCHAR(50) NOT NULL COMMENT '更新人用户名',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0=未删除,1=已删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subscription_name` (`name`),
  KEY `idx_subscription_status` (`status`),
  KEY `idx_subscription_type` (`type`),
  KEY `idx_subscription_valid` (`valid_from`, `valid_to`),
  KEY `idx_subscription_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订阅表';

-- 3. 创建节点订阅关联表
CREATE TABLE `node_subscription_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `node_id` BIGINT NOT NULL COMMENT '节点ID',
  `subscription_id` BIGINT NOT NULL COMMENT '订阅ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` VARCHAR(50) NOT NULL COMMENT '创建人用户名',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0=未删除,1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_subscription` (`node_id`, `subscription_id`, `deleted`),
  KEY `idx_relation_node_id` (`node_id`),
  KEY `idx_relation_subscription_id` (`subscription_id`),
  KEY `idx_relation_deleted` (`deleted`),
  CONSTRAINT `fk_relation_node` FOREIGN KEY (`node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_relation_subscription` FOREIGN KEY (`subscription_id`) REFERENCES `subscription` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节点订阅关联表';

-- ============================================================================
-- 执行说明:
-- 1. 请确保数据库字符集为utf8mb4
-- 2. 建议在测试环境先执行验证
-- 3. 生产环境执行前请备份数据库
-- 4. 外键约束会自动级联删除，请谨慎操作删除节点和订阅
-- ============================================================================
