package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.vo.SubscriptionWithBindingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NodeMapper extends BaseMapper<Node> {
    /**
     * 查询节点绑定的订阅列表（分页）
     */
    IPage<SubscriptionWithBindingVO> selectSubscriptionsByNodeId(IPage<SubscriptionWithBindingVO> page, @Param("nodeId") Long nodeId);

    /**
     * 统计节点的订阅数量
     */
    Integer countSubscriptionsByNodeId(@Param("nodeId") Long nodeId);
}
