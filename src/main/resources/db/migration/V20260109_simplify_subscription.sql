-- 简化订阅表结构：移除type、有效期、流量限制、状态、username等字段，只保留订阅组、订单号及审计字段
-- 1. 重命名name为group_name
ALTER TABLE `subscription`
CHANGE COLUMN `name` `group_name` VARCHAR(100) NOT NULL COMMENT '订阅组名称';

-- 2. 删除不需要的字段
ALTER TABLE `subscription`
DROP COLUMN `type`,
DROP COLUMN `valid_from`,
DROP COLUMN `valid_to`,
DROP COLUMN `status`,
DROP COLUMN `username`,
DROP INDEX `uk_subscription_name`,
DROP INDEX `idx_subscription_status`,
DROP INDEX `idx_subscription_type`,
DROP INDEX `idx_subscription_valid`,
DROP INDEX `idx_username`;

-- 3. 添加新的唯一索引
ALTER TABLE `subscription`
ADD UNIQUE KEY `uk_subscription_group_name` (`group_name`);
