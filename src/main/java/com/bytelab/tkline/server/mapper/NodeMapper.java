package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NodeMapper extends BaseMapper<Node> {
    /**
     * 查询节点绑定的订阅列表（分页）
     */
    IPage<Subscription> selectSubscriptionsByNodeId(IPage<Subscription> page, @Param("nodeId") Long nodeId);
}
