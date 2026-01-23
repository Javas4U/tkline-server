-- ============================================
-- 节点管理系统 - 更新节点状态字段注释
-- 创建时间: 2026-01-19
-- 说明: 将 status 字段的语义从"启用/禁用"改为"离线/在线"
-- ============================================

-- 修改 status 字段的注释，明确表示在线/离线状态
ALTER TABLE `node`
MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0=离线,1=在线';

-- 验证表结构
SHOW COLUMNS FROM `node`;
