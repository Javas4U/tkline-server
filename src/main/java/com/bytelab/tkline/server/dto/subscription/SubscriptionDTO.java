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

    private Integer version;
}
