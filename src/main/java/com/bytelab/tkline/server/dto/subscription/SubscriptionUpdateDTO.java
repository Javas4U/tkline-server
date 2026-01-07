package com.bytelab.tkline.server.dto.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubscriptionUpdateDTO {
    @NotNull(message = "订阅ID不能为空")
    private Long id;

    /**
     * 订阅名称
     */
    private String name;

    /**
     * 订阅类型 (MONTHLY, YEARLY, TRAFFIC, PERMANENT)
     */
    private String type;

    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "有效期至")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    @Schema(description = "流量限制(字节)")
    private Long trafficLimit;

    /**
     * 描述
     */
    private String description;

    @Schema(description = "绑定的节点ID列表")
    private List<Long> nodeIds;

    @NotNull(message = "版本号不能为空")
    private Integer version;
}
