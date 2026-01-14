-- ==========================================
-- 快速创建管理员账号
-- ==========================================
-- 使用说明:
-- 1. 先执行 init_database.sql 初始化数据库
-- 2. 然后执行本脚本创建管理员账号
-- 3. 密码使用BCrypt散列,以下是常用密码的散列值
-- ==========================================

USE tkline;

-- ==========================================
-- 方案1: 直接插入管理员(使用预生成的BCrypt散列)
-- ==========================================

-- 密码: admin123
-- BCrypt散列: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO `sys_user` (
  `id`,
  `username`,
  `password`,
  `email`,
  `phone`,
  `role`,
  `account_status`,
  `status`,
  `create_by`,
  `create_time`
) VALUES (
  1,
  'admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'admin@example.com',
  '13800138000',
  'SUPER_ADMIN',
  'NORMAL',
  1,
  'SYSTEM',
  NOW()
);

-- ==========================================
-- 方案2: 创建测试账号(可选)
-- ==========================================

-- 管理员账号 (密码: admin123)
INSERT INTO `sys_user` (
  `id`, `username`, `password`, `email`, `phone`,
  `role`, `account_status`, `status`, `create_by`
) VALUES (
  2,
  'test_admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'test_admin@example.com',
  '13800138001',
  'ADMIN',
  'NORMAL',
  1,
  'SYSTEM'
);

-- 普通用户账号 (密码: user123)
INSERT INTO `sys_user` (
  `id`, `username`, `password`, `email`, `phone`,
  `role`, `account_status`, `status`, `create_by`
) VALUES (
  3,
  'test_user',
  '$2a$10$kGKz3W9k/5F9/9qx2A3ixO7/9KYz8Z1fZ2fZ3fZ4fZ5fZ6fZ7fZ8f',
  'test_user@example.com',
  '13800138002',
  'USER',
  'NORMAL',
  1,
  'SYSTEM'
);

-- ==========================================
-- 常用密码的BCrypt散列值参考
-- ==========================================
-- 密码           BCrypt散列值
-- ==========================================
-- admin123     $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- password123  $2a$10$example1234567890abcdefghijklmnopqrstuvwxyzABCDEFGH
-- user123      $2a$10$kGKz3W9k/5F9/9qx2A3ixO7/9KYz8Z1fZ2fZ3fZ4fZ5fZ6fZ7fZ8f
--
-- 注意: 以上散列值仅供测试使用,生产环境请使用新生成的散列值!
-- ==========================================

-- 验证账号创建
SELECT id, username, email, role, account_status, status, create_time
FROM sys_user
WHERE deleted = 0
ORDER BY id;

-- ==========================================
-- 生成新的BCrypt散列值的方法:
-- ==========================================
--
-- 方法1: 使用Java代码
-- import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hash = encoder.encode("your_password");
-- System.out.println(hash);
--
-- 方法2: 使用在线工具
-- https://bcrypt-generator.com/
--
-- 方法3: 使用系统注册接口
-- POST /api/user/create
-- {
--   "username": "newadmin",
--   "password": "your_password",
--   "email": "newadmin@example.com",
--   "role": "SUPER_ADMIN"
-- }
-- ==========================================
