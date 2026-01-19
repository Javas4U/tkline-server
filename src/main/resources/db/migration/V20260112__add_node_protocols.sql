-- 添加节点支持的协议字段
ALTER TABLE `node` ADD COLUMN IF NOT EXISTS `protocols` JSON NULL COMMENT '支持的协议列表，JSON数组如 [\"hy2\",\"vless\",\"trojan\"]' AFTER `port`;