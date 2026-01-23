package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * Rule Provider 规则提供者实体
 * 用于 Clash 配置的 rule-providers 配置项
 */
@Data
@TableName("rule_provider")
public class RuleProvider extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称/标识符 (例如: geosite-tiktok)
     */
    @TableField("rule_name")
    private String name;

    /**
     * 规则类型 (http/file)
     */
    @TableField("rule_type")
    private String type;

    /**
     * 行为类型 (domain/ipcidr/classical)
     */
    @TableField("rule_behavior")
    private String behavior;

    /**
     * 格式类型 (mrs/yaml/text)
     */
    @TableField("rule_format")
    private String format;

    /**
     * 规则源URL
     */
    @TableField("rule_url")
    private String url;

    /**
     * 本地存储路径
     */
    @TableField("rule_path")
    private String path;

    /**
     * 更新间隔(秒)
     */
    @TableField("update_interval")
    private Integer updateInterval;

    /**
     * 描述信息
     */
    @TableField("rule_desc")
    private String description;

    /**
     * 规则策略 (PROXY/DIRECT/REJECT)
     */
    @TableField("rule_policy")
    private String policy;

    /**
     * 启用状态 (0-禁用, 1-启用)
     */
    @TableField("enabled")
    private Integer status;

    /**
     * 排序序号(用于控制在配置中的顺序)
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
