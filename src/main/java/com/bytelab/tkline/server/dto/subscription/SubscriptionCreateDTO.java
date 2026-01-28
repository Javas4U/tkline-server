package com.bytelab.tkline.server.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SubscriptionCreateDTO {
    /**
     * 订阅组名称
     */
    @Schema(description = "订阅组名称")
    private String groupName;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "是否付费用户:0=未付费,1=已付费")
    private Integer isPaid;

    @Schema(description = "备注")
    private String description;
}
