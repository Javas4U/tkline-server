package com.bytelab.tkline.server.dto.node;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NodeUpdateDTO {
    @NotNull(message = "节点ID不能为空")
    private Long id;

    @NotBlank(message = "节点名称不能为空")
    @Size(max = 100, message = "节点名称长度不能超过100")
    private String name;

    @NotBlank(message = "域名不能为空")
    @Size(max = 255, message = "域名长度不能超过255")
    private String domain;

    @Size(max = 45, message = "IP地址长度不能超过45")
    private String ipAddress;

    @NotNull(message = "端口号不能为空")
    @Min(value = 1, message = "端口号必须在1-65535之间")
    @Max(value = 65535, message = "端口号必须在1-65535之间")
    private Integer port;

    @Size(max = 50, message = "地区长度不能超过50")
    private String region;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

    @Size(max = 1000, message = "协议列表JSON长度不能超过1000")
    private String protocols;

    @Min(value = 0, message = "上行配额不能为负数")
    private Integer upstreamQuota;

    @Min(value = 0, message = "下行配额不能为负数")
    private Integer downstreamQuota;

    private Boolean online;

}
