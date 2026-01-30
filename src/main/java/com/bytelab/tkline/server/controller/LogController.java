package com.bytelab.tkline.server.controller;

import com.bytelab.tkline.server.dto.log.AccessLogReportDTO;
import com.bytelab.tkline.server.service.UserAccessLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "日志管理")
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final UserAccessLogService userAccessLogService;

    @Operation(summary = "上报用户访问日志")
    @PostMapping("/report/access")
    public void reportAccessLog(@RequestBody AccessLogReportDTO reportDTO) {
        userAccessLogService.processLogReport(reportDTO);
    }
}
