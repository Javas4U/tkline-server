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
