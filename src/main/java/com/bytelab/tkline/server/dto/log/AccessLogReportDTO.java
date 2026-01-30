package com.bytelab.tkline.server.dto.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "访问日志上报对象")
public class AccessLogReportDTO {

    @Schema(description = "节点ID")
    private Long nodeId;

    @Schema(description = "日志列表")
    private List<AccessLogItemDTO> logs;
}
