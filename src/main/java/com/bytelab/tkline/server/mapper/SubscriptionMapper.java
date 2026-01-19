package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
    /**
     * 查询订阅绑定的节点列表（分页,包含绑定配置,支持查询条件）
     *
     * @param page 分页参数
     * @param subscriptionId 订阅ID
     * @param name 节点名称(模糊查询)
     * @param region 区域(模糊查询)
     * @return 节点列表
     */
    IPage<NodeDTO> selectNodesBySubscriptionId(
            IPage<NodeDTO> page,
            @Param("subscriptionId") Long subscriptionId,
            @Param("name") String name,
            @Param("region") String region);
}
