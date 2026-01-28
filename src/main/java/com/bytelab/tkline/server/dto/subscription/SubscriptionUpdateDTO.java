package com.bytelab.tkline.server.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionUpdateDTO {
    @NotNull(message = "订阅ID不能为空")
    private Long id;

    /**
     * 订阅组名称
     */
    @Schema(description = "订阅组名称")
    private String groupName;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 是否付费用户
     */
    @Schema(description = "是否付费用户:0=未付费,1=已付费")
    private Integer isPaid;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

}
