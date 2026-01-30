package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户访问日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_access_log")
public class UserAccessLog extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名 (冗余)
     */
    @TableField("username")
    private String username;

    /**
     * 节点ID
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 目标地址 (域名或IP)
     */
    @TableField("target_address")
    private String targetAddress;

    /**
     * 地址类型 (1: DOMAIN, 2: IP)
     */
    @TableField("address_type")
    private Integer addressType;

    /**
     * 访问次数
     */
    @TableField("hit_count")
    private Integer hitCount;

    /**
     * 访问时间 (聚合时间段的开始时间)
     */
    @TableField("access_time")
    private LocalDateTime accessTime;
}
