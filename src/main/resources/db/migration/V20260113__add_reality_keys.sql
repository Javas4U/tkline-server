-- 添加 Reality 协议公私钥字段
ALTER TABLE `node`
ADD COLUMN `reality_public_key` VARCHAR(255) NULL COMMENT 'Reality 协议公钥' AFTER `protocols`,
ADD COLUMN `reality_private_key` VARCHAR(255) NULL COMMENT 'Reality 协议私钥' AFTER `reality_public_key`;
