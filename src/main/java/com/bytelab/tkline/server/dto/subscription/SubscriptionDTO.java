package com.bytelab.tkline.server.dto.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    private Long id;
    private String groupName;
    private String description;
    private String orderNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;

    @Schema(description = "总节点数")
    private Integer nodeCount;

    @Schema(description = "可用节点数")
    private Integer availableNodeCount;

    // 绑定配置信息(仅在查询节点订阅时返回)
    @Schema(description = "订阅开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "订阅结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    @Schema(description = "流量限制(字节)")
    private Long trafficLimit;

    @Schema(description = "绑定状态")
    private Integer bindingStatus;

}
