package com.bytelab.tkline.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytelab.tkline.server.dto.log.AccessLogReportDTO;
import com.bytelab.tkline.server.entity.UserAccessLog;

public interface UserAccessLogService extends IService<UserAccessLog> {

    /**
     * 处理日志上报
     *
     * @param reportDTO 日志上报数据
     */
    void processLogReport(AccessLogReportDTO reportDTO);
}
