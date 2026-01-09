package com.bytelab.tkline.server.dto.relation;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点订阅绑定DTO - 包含有效期、流量和状态信息
 */
@Data
public class NodeSubscriptionBindDTO {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "订阅ID")
    private Long subscriptionId;

    @Schema(description = "有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    @Schema(description = "流量限制(字节)")
    private Long trafficLimit;

    @Schema(description = "状态:0=禁用,1=有效,2=过期")
    private Integer status;
}
