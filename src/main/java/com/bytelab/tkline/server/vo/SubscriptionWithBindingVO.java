package com.bytelab.tkline.server.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订阅与绑定信息组合VO
 * 用于查询节点的订阅列表时,同时返回订阅基本信息和绑定配置
 */
@Data
public class SubscriptionWithBindingVO {
    // 订阅基本信息
    private Long id;
    private String groupName;
    private String description;
    private String orderNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;

    // 绑定配置信息(来自node_subscription_relation表)
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Long trafficLimit;
    private Integer bindingStatus;
}
