package com.bytelab.tkline.server.dto.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "单条访问日志对象")
public class AccessLogItemDTO {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "目标地址")
    private String address;

    @Schema(description = "地址类型 (DOMAIN/IP)")
    private String type;

    @Schema(description = "访问次数")
    private Integer hits;

    @Schema(description = "记录时间")
    private LocalDateTime timestamp;
}
