package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("type")
    private String type;

    @TableField("valid_from")
    private LocalDateTime validFrom;

    @TableField("valid_to")
    private LocalDateTime validTo;

    @TableField("traffic_limit")
    private Long trafficLimit;

    @TableField("traffic_used")
    private Long trafficUsed;

    @TableField("status")
    private Integer status;

    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
