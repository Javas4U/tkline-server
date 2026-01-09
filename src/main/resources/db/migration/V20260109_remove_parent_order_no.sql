-- 从node_subscription_relation表移除parent_order_no字段
ALTER TABLE `node_subscription_relation`
DROP COLUMN `parent_order_no`,
DROP INDEX `idx_order_no`;

-- 重新添加order_no的索引
ALTER TABLE `node_subscription_relation`
ADD INDEX `idx_order_no` (`order_no`);
