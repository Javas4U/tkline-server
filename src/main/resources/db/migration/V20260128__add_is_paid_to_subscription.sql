-- 为subscription表添加is_paid字段以标识付费用户
-- 添加付费标识字段
ALTER TABLE `subscription`
ADD COLUMN `is_paid` TINYINT NOT NULL DEFAULT 0 COMMENT '是否付费用户:0=未付费,1=已付费' AFTER `order_no`;

-- 为is_paid字段创建索引以便快速查询
ALTER TABLE `subscription`
ADD INDEX `idx_subscription_is_paid` (`is_paid`);
