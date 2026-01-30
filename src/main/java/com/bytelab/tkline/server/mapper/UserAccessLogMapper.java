package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytelab.tkline.server.entity.UserAccessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAccessLogMapper extends BaseMapper<UserAccessLog> {
}
