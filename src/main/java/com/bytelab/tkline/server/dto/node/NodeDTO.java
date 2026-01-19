package com.bytelab.tkline.server.dto.node;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Schema(description = "支持的协议列表，如 [\"hy2\",\"vmess\",\"trojan\"]")
    private String protocols;

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

    // 绑定配置信息(仅在查询订阅节点时返回)
    @Schema(description = "绑定有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @Schema(description = "绑定有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    @Schema(description = "绑定流量限制(GB)")
    private Long trafficLimit;

    @Schema(description = "绑定状态:0=禁用,1=有效,2=过期")
    private Integer bindingStatus;
}
