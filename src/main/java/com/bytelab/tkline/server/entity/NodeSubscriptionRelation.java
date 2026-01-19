package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("node_subscription_relation")
public class NodeSubscriptionRelation {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("node_id")
    private Long nodeId;

    @TableField("subscription_id")
    private Long subscriptionId;

    @TableField("order_no")
    private String orderNo;

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

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
