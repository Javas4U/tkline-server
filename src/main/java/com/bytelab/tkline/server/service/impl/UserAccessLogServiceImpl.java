package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.dto.log.AccessLogItemDTO;
import com.bytelab.tkline.server.dto.log.AccessLogReportDTO;
import com.bytelab.tkline.server.entity.SysUser;
import com.bytelab.tkline.server.entity.UserAccessLog;
import com.bytelab.tkline.server.mapper.UserMapper;
import com.bytelab.tkline.server.mapper.UserAccessLogMapper;
import com.bytelab.tkline.server.service.UserAccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccessLogServiceImpl extends ServiceImpl<UserAccessLogMapper, UserAccessLog>
        implements UserAccessLogService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processLogReport(AccessLogReportDTO reportDTO) {
        log.info("Received log report from node: {}, count: {}", reportDTO.getNodeId(), reportDTO.getLogs().size());

        if (reportDTO.getLogs().isEmpty()) {
            return;
        }

        // 1. 获取所有涉及的用户名，批量查询用户ID
        List<String> usernames = reportDTO.getLogs().stream()
                .map(AccessLogItemDTO::getUsername)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Long> userMap = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getUsername, usernames))
                .stream()
                .collect(Collectors.toMap(SysUser::getUsername, SysUser::getId));

        // 2. 转换实体
        List<UserAccessLog> entities = new ArrayList<>();
        for (AccessLogItemDTO item : reportDTO.getLogs()) {
            UserAccessLog logEntity = new UserAccessLog();
            logEntity.setNodeId(reportDTO.getNodeId());
            logEntity.setUsername(item.getUsername());
            logEntity.setUserId(userMap.getOrDefault(item.getUsername(), 0L)); // 如果找不到用户，记录为0
            logEntity.setTargetAddress(item.getAddress());
            logEntity.setAddressType("IP".equalsIgnoreCase(item.getType()) ? 2 : 1);
            logEntity.setHitCount(item.getHits());
            logEntity.setAccessTime(item.getTimestamp());
            entities.add(logEntity);
        }

        // 3. 批量保存
        // MyBatis Plus 的 saveBatch 默认是 1000 条一次，这里可以直接调用
        this.saveBatch(entities);

        log.info("Saved {} access logs", entities.size());
    }
}
