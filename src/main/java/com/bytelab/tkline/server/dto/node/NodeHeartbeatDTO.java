package com.bytelab.tkline.server.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "节点心跳DTO")
public class NodeHeartbeatDTO {

    @Schema(description = "节点ID")
    @NotNull(message = "节点ID不能为空")
    private Long id;
}
