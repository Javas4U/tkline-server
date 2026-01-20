package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("node")
public class Node {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("domain")
    private String domain;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("port")
    private Integer port;

    @TableField("region")
    private String region;

    @TableField("status")
    private Integer status;

    @TableField("description")
    private String description;

    @TableField("protocols")
    private String protocols;

    @TableField("upstream_quota")
    private Integer upstreamQuota;

    @TableField("downstream_quota")
    private Integer downstreamQuota;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
