-- ============================================
-- Rule Provider 规则提供者表创建脚本
-- 用于管理 Clash 配置的 rule-providers
-- ============================================

CREATE TABLE IF NOT EXISTS `rule_provider` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称/标识符 (例如: geosite-tiktok)',
    `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型 (http/file)',
    `rule_behavior` VARCHAR(20) NOT NULL COMMENT '行为类型 (domain/ipcidr/classical)',
    `rule_format` VARCHAR(20) NOT NULL COMMENT '格式类型 (mrs/yaml/text)',
    `rule_url` VARCHAR(500) NOT NULL COMMENT '规则源URL',
    `rule_path` VARCHAR(200) NOT NULL COMMENT '本地存储路径',
    `update_interval` INT NOT NULL DEFAULT 86400 COMMENT '更新间隔(秒)',
    `rule_desc` VARCHAR(500) DEFAULT NULL COMMENT '描述信息',
    `rule_policy` VARCHAR(20) NOT NULL DEFAULT 'PROXY' COMMENT '规则策略 (PROXY/DIRECT/REJECT)',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 (0-禁用, 1-启用)',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号(用于控制在配置中的顺序)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_name` (`rule_name`, `deleted`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Rule Provider 规则提供者表';

-- ============================================
-- 插入默认数据 (原有的硬编码规则)
-- ============================================

INSERT INTO `rule_provider` (`rule_name`, `rule_type`, `rule_behavior`, `rule_format`, `rule_url`, `rule_path`, `update_interval`, `rule_desc`, `rule_policy`, `enabled`, `sort_order`) VALUES
('geosite-category-ads-all', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/category-ads-all.mrs', './ruleset/geosite-category-ads-all.mrs', 86400, '广告拦截规则', 'REJECT', 1, 0),
('geosite-tiktok', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/tiktok.mrs', './ruleset/geosite-tiktok.mrs', 86400, 'TikTok 域名规则', 'PROXY', 1, 1),
('geosite-youtube', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/youtube.mrs', './ruleset/geosite-youtube.mrs', 86400, 'YouTube 域名规则', 'PROXY', 1, 2),
('geosite-google', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/google.mrs', './ruleset/geosite-google.mrs', 86400, 'Google 域名规则', 'PROXY', 1, 3),
('geosite-instagram', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/instagram.mrs', './ruleset/geosite-instagram.mrs', 86400, 'Instagram 域名规则', 'PROXY', 1, 4),
('geosite-facebook', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/facebook.mrs', './ruleset/geosite-facebook.mrs', 86400, 'Facebook 域名规则', 'PROXY', 1, 5),
('geosite-twitter', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/twitter.mrs', './ruleset/geosite-twitter.mrs', 86400, 'Twitter 域名规则', 'PROXY', 1, 6),
('geosite-netflix', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/netflix.mrs', './ruleset/geosite-netflix.mrs', 86400, 'Netflix 域名规则', 'PROXY', 1, 7),
('geosite-openai', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/openai.mrs', './ruleset/geosite-openai.mrs', 86400, 'OpenAI 域名规则', 'PROXY', 1, 8),
('geosite-telegram', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/telegram.mrs', './ruleset/geosite-telegram.mrs', 86400, 'Telegram 域名规则', 'PROXY', 1, 9),
('geosite-spotify', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/spotify.mrs', './ruleset/geosite-spotify.mrs', 86400, 'Spotify 域名规则', 'PROXY', 1, 10),
('geosite-github', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/github.mrs', './ruleset/geosite-github.mrs', 86400, 'GitHub 域名规则', 'PROXY', 1, 11),
('geosite-linkedin', 'http', 'domain', 'mrs', 'https://raw.githubusercontent.com/MetaCubeX/meta-rules-dat/meta/geo/geosite/linkedin.mrs', './ruleset/geosite-linkedin.mrs', 86400, 'LinkedIn 域名规则', 'PROXY', 1, 12);
