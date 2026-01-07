package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
    /**
     * 查询订阅绑定的节点列表（分页）
     */
    IPage<Node> selectNodesBySubscriptionId(IPage<Node> page, @Param("subscriptionId") Long subscriptionId);
}
