package com.bytelab.tkline.server.dto.relation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RelationBindDTO {
    @NotNull(message = "节点ID不能为空")
    private Long nodeId;

    @NotNull(message = "订阅ID不能为空")
    @Size(min = 1, message = "至少选择一个订阅")
    private List<Long> subscriptionIds;
}
