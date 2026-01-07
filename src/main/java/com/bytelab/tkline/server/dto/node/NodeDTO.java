package com.bytelab.tkline.server.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NodeDTO {
    private Long id;
    private String name;
    private String ipAddress;
    private Integer port;
    private String region;
    private Integer status;
    private String statusLabel;
    private String description;

    @Schema(description = "上行配额(Mbps)")
    private Integer upstreamQuota;

    @Schema(description = "下行配额(Mbps)")
    private Integer downstreamQuota;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    @Schema(description = "是否在线")
    private Boolean online;
    private Integer subscriptionCount;
}
