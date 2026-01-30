package com.bytelab.tkline.server.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.log.AccessLogReportDTO;
import com.bytelab.tkline.server.dto.log.UserAccessLogQueryDTO;
import com.bytelab.tkline.server.entity.UserAccessLog;
import com.bytelab.tkline.server.service.UserAccessLogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class UserAccessLogController {

    private final UserAccessLogService userAccessLogService;

    /**
     * 分页查询访问日志
     */
    @PostMapping("/page")
    public ApiResult<IPage<UserAccessLog>> page(@RequestBody UserAccessLogQueryDTO queryDTO) {
        IPage<UserAccessLog> page = userAccessLogService.pageQuery(queryDTO);
        return ApiResult.success(page);
    }

    @Operation(summary = "上报用户访问日志")
    @PostMapping("/report/access")
    public void reportAccessLog(@RequestBody AccessLogReportDTO reportDTO) {
        userAccessLogService.processLogReport(reportDTO);
    }
}
