-- ==========================================
-- TKLine 用户认证和管理系统 - 数据库初始化脚本
-- ==========================================
-- 版本: 1.0
-- 创建时间: 2025-01-05
-- 说明: 精简版系统,只保留用户认证和管理核心功能
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS tkline
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE tkline;

-- ==========================================
-- 表1: sys_user - 用户主表
-- ==========================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt散列)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: SUPER_ADMIN-超级管理员, ADMIN-管理员, USER-普通用户',

  -- 审计字段
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人用户名',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`),
  KEY `idx_account_status` (`account_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

-- ==========================================
-- 表2: user_login_log - 用户登录日志
-- ==========================================
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
  `login_location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
  `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型: PC/Mobile/Tablet',
  `device_id` VARCHAR(100) DEFAULT NULL COMMENT '设备唯一标识',
  `login_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '登录状态: 0-失败, 1-成功',
  `login_type` VARCHAR(20) DEFAULT 'PASSWORD' COMMENT '登录类型: PASSWORD-密码登录, TOKEN-Token登录',
  `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `token` TEXT DEFAULT NULL COMMENT 'JWT Token',
  `token_expire_time` DATETIME DEFAULT NULL COMMENT 'Token过期时间',
  `logout_time` DATETIME DEFAULT NULL COMMENT '登出时间',
  `online_duration` INT DEFAULT NULL COMMENT '在线时长(秒)',

  -- 审计字段
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(登录时间)',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人用户名',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',

  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_status` (`login_status`),
  KEY `idx_login_ip` (`login_ip`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志';

-- ==========================================
-- 表3: user_device - 用户设备
-- ==========================================
DROP TABLE IF EXISTS `user_device`;
CREATE TABLE `user_device` (
  `id` BIGINT NOT NULL COMMENT '设备ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型: IOS, ANDROID, WINDOWS, MAC',
  `device_id` VARCHAR(100) NOT NULL COMMENT '设备唯一标识',
  `device_model` VARCHAR(100) DEFAULT NULL COMMENT '设备型号',
  `os_version` VARCHAR(50) DEFAULT NULL COMMENT '操作系统版本',
  `app_version` VARCHAR(50) DEFAULT NULL COMMENT '应用版本',
  `last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线时间',
  `last_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后连接IP',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',

  -- 审计字段
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人用户名',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',

  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_last_online_time` (`last_online_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备信息';

-- ==========================================
-- 表4: rsa_key_pair - RSA密钥对
-- ==========================================
DROP TABLE IF EXISTS `rsa_key_pair`;
CREATE TABLE `rsa_key_pair` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_id` VARCHAR(50) NOT NULL COMMENT '密钥唯一标识(UUID)',
  `public_key` TEXT NOT NULL COMMENT 'RSA公钥(Base64编码)',
  `private_key` TEXT NOT NULL COMMENT 'RSA私钥(Base64编码)',
  `key_size` INT NOT NULL DEFAULT 2048 COMMENT '密钥长度(位): 1024, 2048, 4096',
  `algorithm` VARCHAR(20) NOT NULL DEFAULT 'RSA' COMMENT '加密算法',
  `version` INT NOT NULL DEFAULT 1 COMMENT '密钥版本号',
  `is_active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为活跃密钥: 0-否, 1-是',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
  `usage_count` BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '密钥描述',

  -- 审计字段
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人用户名',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_id` (`key_id`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RSA密钥对管理';

-- ==========================================
-- 表5: system_config - 系统配置
-- ==========================================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `config_type` VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型: STRING, NUMBER, BOOLEAN, JSON',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
  `is_public` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开: 0-私有, 1-公开',

  -- 审计字段
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人用户名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) DEFAULT NULL COMMENT '修改人用户名',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

-- ==========================================
-- 初始数据
-- ==========================================

-- 插入超级管理员账号
-- 默认密码: admin123 (BCrypt散列后的值需要通过程序生成)
-- 注意: 实际使用时,请通过程序的注册接口或UserService创建,这里只是示例
INSERT INTO `sys_user` (`id`, `username`, `password`, `email`, `role`, `account_status`, `status`, `create_by`, `create_time`)
VALUES
  (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@example.com', 'SUPER_ADMIN', 'NORMAL', 1, 'SYSTEM', NOW());

-- 插入系统配置示例
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `is_public`, `create_by`)
VALUES
  ('system.name', 'TKLine用户管理系统', 'STRING', '系统名称', 1, 'SYSTEM'),
  ('system.version', '1.0.0', 'STRING', '系统版本', 1, 'SYSTEM'),
  ('jwt.expire.hours', '24', 'NUMBER', 'JWT Token过期时间(小时)', 0, 'SYSTEM'),
  ('login.max.fail.count', '5', 'NUMBER', '登录最大失败次数', 0, 'SYSTEM'),
  ('login.lock.duration.minutes', '15', 'NUMBER', '账号锁定时长(分钟)', 0, 'SYSTEM'),
  ('password.min.length', '8', 'NUMBER', '密码最小长度', 0, 'SYSTEM'),
  ('rsa.key.expire.days', '30', 'NUMBER', 'RSA密钥过期天数', 0, 'SYSTEM');

-- ==========================================
-- 数据库初始化完成
-- ==========================================
-- 说明:
-- 1. 所有表都支持逻辑删除(deleted字段)
-- 2. 所有表都包含审计字段(create_by, create_time, update_by, update_time)
-- 3. sys_user.password字段存储BCrypt散列值,不可逆加密
-- 4. 角色变更和黑名单操作直接在sys_user表的对应字段维护,不需要单独日志表
-- 5. 建议在生产环境中修改超级管理员的默认密码
-- ==========================================
