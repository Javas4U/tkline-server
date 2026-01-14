# TKLine 数据库初始化说明

## 概述

本目录包含TKLine用户认证和管理系统的数据库初始化脚本。

## 文件说明

- `init_database.sql` - 完整的数据库初始化脚本,包含所有表结构和初始数据

## 数据表清单

系统共包含 **5张核心数据表**:

### 1. sys_user - 用户主表
存储用户基本信息,包括用户名、密码(BCrypt散列)、邮箱、手机号、角色、账号状态等。

**关键字段:**
- `username` - 用户名(唯一)
- `password` - BCrypt散列密码
- `email` - 邮箱(唯一)
- `role` - 角色(SUPER_ADMIN/ADMIN/USER)
- `account_status` - 账号状态(NORMAL/BLACKLISTED)

**说明:** 角色变更和黑名单操作直接更新此表的对应字段,不需要单独的日志表

### 2. user_login_log - 用户登录日志
记录所有用户的登录尝试,包括成功和失败的登录,记录IP、设备、浏览器等信息。

**关键字段:**
- `login_status` - 登录状态(0-失败, 1-成功)
- `login_type` - 登录类型(PASSWORD/TOKEN)
- `token` - JWT Token
- `online_duration` - 在线时长

### 3. user_device - 用户设备
记录用户的设备信息,支持多设备管理。

**关键字段:**
- `device_id` - 设备唯一标识
- `device_type` - 设备类型(IOS/ANDROID/WINDOWS/MAC)
- `last_online_time` - 最后在线时间

### 4. rsa_key_pair - RSA密钥对
存储系统的RSA公私钥对,用于敏感数据的加密传输。

**关键字段:**
- `key_id` - 密钥唯一标识
- `public_key` - RSA公钥(Base64)
- `private_key` - RSA私钥(Base64)
- `is_active` - 是否为活跃密钥
- `usage_count` - 使用次数

### 5. system_config - 系统配置
存储系统级配置参数。

**关键字段:**
- `config_key` - 配置键(唯一)
- `config_value` - 配置值
- `config_type` - 配置类型(STRING/NUMBER/BOOLEAN/JSON)

## 使用方法

### 1. 创建数据库并初始化

```bash
# 方式1: 使用MySQL命令行
mysql -u root -p < init_database.sql

# 方式2: 登录MySQL后执行
mysql -u root -p
mysql> source /path/to/init_database.sql
```

### 2. 验证数据库创建

```sql
-- 切换到tkline数据库
USE tkline;

-- 查看所有表
SHOW TABLES;

-- 查看表结构示例
DESC sys_user;

-- 查看初始数据
SELECT * FROM sys_user;
SELECT * FROM system_config;
```

## 初始数据说明

### 默认管理员账号

脚本中包含一个示例管理员账号(需要替换密码):

```
用户名: admin
密码: admin123 (需要通过BCrypt散列后替换)
邮箱: admin@example.com
角色: SUPER_ADMIN
```

**重要提示:**
- 示例中的BCrypt散列值是占位符,实际使用时请通过以下方式之一创建:
  1. 使用系统的注册接口创建
  2. 使用 `UserService.createUser()` 方法创建
  3. 使用在线BCrypt工具生成散列值后手动更新

### 生成BCrypt密码散列

可以使用以下Java代码生成BCrypt散列:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("admin123");
System.out.println(hashedPassword);
// 输出类似: $2a$10$abcd1234...
```

然后将生成的散列值更新到SQL脚本中:

```sql
UPDATE sys_user
SET password = '$2a$10$your_generated_hash_here'
WHERE username = 'admin';
```

### 系统配置参数

初始化脚本包含以下系统配置:

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| system.name | TKLine用户管理系统 | 系统名称 |
| system.version | 1.0.0 | 系统版本 |
| jwt.expire.hours | 24 | JWT Token过期时间(小时) |
| login.max.fail.count | 5 | 登录最大失败次数 |
| login.lock.duration.minutes | 15 | 账号锁定时长(分钟) |
| password.min.length | 8 | 密码最小长度 |
| rsa.key.expire.days | 30 | RSA密钥过期天数 |

## 数据库设计特点

### 1. 逻辑删除
所有主表都支持逻辑删除(`deleted`字段):
- `0` - 未删除(正常)
- `1` - 已删除

查询时自动过滤已删除数据(MyBatis-Plus的`@TableLogic`注解)。

### 2. 审计字段
所有主表都包含审计字段:
- `create_by` - 创建人用户名
- `create_time` - 创建时间
- `update_by` - 修改人用户名
- `update_time` - 修改时间

这些字段由`AuditMetaObjectHandler`自动填充。

### 3. 只追加日志表
~~以下表是只追加(append-only)的日志表,不允许修改和删除:~~
- ~~`role_change_log` - 角色变更日志~~
- ~~`blacklist_log` - 黑名单操作日志~~

**说明:** 系统已简化,角色变更和黑名单操作直接更新`sys_user`表的对应字段

### 4. 字符集和排序规则
- 字符集: `utf8mb4`
- 排序规则: `utf8mb4_unicode_ci`
- 支持完整的Unicode字符,包括emoji

### 5. 索引设计
每个表都设计了合适的索引:
- 主键索引(PRIMARY KEY)
- 唯一索引(UNIQUE KEY) - 如username, email
- 普通索引(KEY) - 常用查询字段

## 注意事项

### 安全建议

1. **修改默认密码**: 初始化后立即修改admin账号的密码
2. **数据库权限**: 生产环境使用专用数据库用户,不要使用root
3. **备份**: 定期备份数据库
4. **SSL连接**: 生产环境启用MySQL SSL连接

### 生产环境配置

在生产环境部署时,建议:

```sql
-- 1. 创建专用数据库用户
CREATE USER 'tkline_user'@'%' IDENTIFIED BY 'strong_password_here';
GRANT SELECT, INSERT, UPDATE, DELETE ON tkline.* TO 'tkline_user'@'%';
FLUSH PRIVILEGES;

-- 2. 配置连接池参数
-- 在application.yml中配置:
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### 性能优化

对于大数据量场景:

1. **定期清理日志表**: 设置日志表的数据保留策略
```sql
-- 删除90天前的登录日志
DELETE FROM user_login_log
WHERE create_time < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

2. **分区表**: 考虑对日志表使用分区(按月或按年)

3. **索引优化**: 根据实际查询需求调整索引

## 故障排查

### 常见问题

#### 1. 字符集问题
```sql
-- 检查数据库字符集
SHOW CREATE DATABASE tkline;

-- 修改表字符集
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 权限问题
```sql
-- 检查用户权限
SHOW GRANTS FOR 'tkline_user'@'%';

-- 授予权限
GRANT ALL PRIVILEGES ON tkline.* TO 'tkline_user'@'%';
```

#### 3. 连接问题
```bash
# 检查MySQL是否运行
systemctl status mysql

# 检查端口
netstat -tlnp | grep 3306

# 测试连接
mysql -h localhost -u tkline_user -p tkline
```

## 数据迁移

如果需要从旧版本迁移:

1. 备份原数据库
2. 导出需要保留的数据
3. 执行`init_database.sql`创建新表结构
4. 使用`INSERT INTO ... SELECT`导入数据
5. 验证数据完整性

## 联系方式

如有问题,请联系开发团队或提交Issue。

---

**版本**: 1.0
**更新时间**: 2025-01-05
**数据库版本**: MySQL 8.0+
