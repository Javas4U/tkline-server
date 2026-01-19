package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytelab.tkline.server.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录日志 Mapper
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}

