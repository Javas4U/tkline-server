package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_name")
    private String groupName;

    @TableField("description")
    private String description;

    @TableField("order_no")
    private String orderNo;

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
