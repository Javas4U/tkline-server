-- 为 node_subscription_relation 表添加更新时间和更新人字段
ALTER TABLE `node_subscription_relation`
ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_by`,
ADD COLUMN `update_by` VARCHAR(50) NULL COMMENT '更新人用户名' AFTER `update_time`;
