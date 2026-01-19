package com.bytelab.tkline.server.dto.relation;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点订阅关联信息DTO
 */
@Data
public class NodeSubscriptionRelationDTO {

    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "订阅ID")
    private Long subscriptionId;

    @Schema(description = "订阅名称")
    private String subscriptionName;

    @Schema(description = "有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    @Schema(description = "流量限制(字节)")
    private Long trafficLimit;

    @Schema(description = "已使用流量(字节)")
    private Long trafficUsed;

    @Schema(description = "状态:0=禁用,1=有效,2=过期")
    private Integer status;

    @Schema(description = "状态标签")
    private String statusLabel;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "是否有效")
    private Boolean isValid;

    @Schema(description = "是否过期")
    private Boolean isExpired;

    @Schema(description = "流量是否耗尽")
    private Boolean isTrafficExhausted;
}
