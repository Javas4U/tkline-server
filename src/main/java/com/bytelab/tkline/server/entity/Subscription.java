package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_name")
    private String groupName;

    @TableField("description")
    private String description;

    @TableField("order_no")
    private String orderNo;
}
