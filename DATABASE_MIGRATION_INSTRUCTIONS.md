# 数据库迁移说明

## 迁移文件位置
`src/main/resources/db/migration/V20260109_add_subscription_order_fields.sql`

## 迁移内容

### 为 subscription 表添加字段
- `username` VARCHAR(64) - 用户名
- `order_no` VARCHAR(128) - 订单号
- 添加索引 `idx_username` 和 `idx_order_no`

### 为 node_subscription_relation 表添加字段
- `order_no` VARCHAR(128) - 子订单号
- 添加索引 `idx_order_no`

## 执行方式

### 方式1: 使用MySQL客户端
```bash
mysql -h 1.94.145.5 -P 3306 -u root -p tkline < src/main/resources/db/migration/V20260109_add_subscription_order_fields.sql
```

### 方式2: 直接执行SQL
连接到数据库后执行以下SQL：

```sql
-- 为subscription表添加username和order_no字段
ALTER TABLE `subscription`
ADD COLUMN `username` VARCHAR(64) NULL COMMENT '用户名' AFTER `description`,
ADD COLUMN `order_no` VARCHAR(128) NULL COMMENT '订单号' AFTER `username`,
ADD INDEX `idx_username` (`username`),
ADD INDEX `idx_order_no` (`order_no`);

-- 为node_subscription_relation表添加order_no字段
ALTER TABLE `node_subscription_relation`
ADD COLUMN `order_no` VARCHAR(128) NULL COMMENT '子订单号' AFTER `subscription_id`,
ADD INDEX `idx_order_no` (`order_no`);
```

## 验证迁移
执行后检查表结构：
```sql
DESC subscription;
DESC node_subscription_relation;
```

## 相关代码修改
- ✅ Subscription 实体类已添加 username 和 orderNo 字段
- ✅ NodeSubscriptionRelation 实体类已添加 orderNo 字段
- ✅ SubscriptionDTO 已添加对应字段
- ✅ SubscriptionOrderGenerator 工具类已创建（生成订单号和用户名）
- ✅ SubscriptionServiceImpl 已修改（创建时自动生成）
- ✅ NodeSubscriptionRelationServiceImpl 已修改（绑定时生成子订单号）
- ✅ SubscriptionController 已添加获取订阅链接API
- ✅ 前端已集成订阅链接功能

## 订单号格式
- 主订单号: `SUB20260109123456789012` (SUBYYYYMMDD + 12位数字)
- 子订单号: `SUB20260109123456789012N0001` (主订单号 + N + 节点ID后4位)
- 用户名: `sub_user_a1b2c3d4` (sub_user_ + 8位随机字符)

## 父子关系
从子订单号可以直接查询到父订单（通过去除最后的 "N####" 部分），无需单独的 parent_order_no 字段。
