-- 移除 Reality 协议公私钥字段
ALTER TABLE `node`
DROP COLUMN `reality_public_key`,
DROP COLUMN `reality_private_key`;
