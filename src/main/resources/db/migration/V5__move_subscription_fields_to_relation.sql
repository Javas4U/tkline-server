-- ============================================================================
-- 数据迁移脚本: 将订阅有效期、流量和状态字段移至关联表
-- 版本: V5
-- 创建时间: 2026-01-08
-- 说明: 将 subscription 表中的 valid_from, valid_to, traffic_limit, traffic_used, status
--       字段迁移到 node_subscription_relation 表中，实现每个节点独立的有效期和流量控制
-- ============================================================================

-- 1. 在 node_subscription_relation 表中添加新字段
ALTER TABLE `node_subscription_relation`
    ADD COLUMN `valid_from` DATETIME NULL COMMENT '有效期开始时间' AFTER `subscription_id`,
    ADD COLUMN `valid_to` DATETIME NULL COMMENT '有效期结束时间' AFTER `valid_from`,
    ADD COLUMN `traffic_limit` BIGINT NULL COMMENT '流量限制(字节)' AFTER `valid_to`,
    ADD COLUMN `traffic_used` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用流量(字节)' AFTER `traffic_limit`,
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0=禁用,1=有效,2=过期' AFTER `traffic_used`;

-- 2. 添加索引优化查询性能
ALTER TABLE `node_subscription_relation`
    ADD KEY `idx_relation_status` (`status`),
    ADD KEY `idx_relation_valid` (`valid_from`, `valid_to`);

-- 3. 数据迁移: 将订阅表中的数据复制到关联表
-- 注意: 此操作将订阅的统一配置应用到所有关联的节点
UPDATE `node_subscription_relation` nsr
INNER JOIN `subscription` s ON nsr.subscription_id = s.id
SET
    nsr.valid_from = s.valid_from,
    nsr.valid_to = s.valid_to,
    nsr.traffic_limit = s.traffic_limit,
    nsr.traffic_used = s.traffic_used,
    nsr.status = s.status
WHERE nsr.deleted = 0 AND s.deleted = 0;

-- 4. 从订阅表中删除已迁移的字段
ALTER TABLE `subscription`
    DROP COLUMN `valid_from`,
    DROP COLUMN `valid_to`,
    DROP COLUMN `traffic_limit`,
    DROP COLUMN `traffic_used`,
    DROP COLUMN `status`;

-- 5. 删除订阅表中不再需要的索引
ALTER TABLE `subscription`
    DROP INDEX `idx_subscription_status`,
    DROP INDEX `idx_subscription_valid`;

-- ============================================================================
-- 回滚说明:
-- 如需回滚此迁移，请执行以下步骤：
-- 1. 在 subscription 表中重新添加已删除的字段
-- 2. 从 node_subscription_relation 表复制数据回 subscription 表(需要聚合逻辑)
-- 3. 从 node_subscription_relation 表删除新增字段
-- 4. 重建 subscription 表的索引
-- ============================================================================
