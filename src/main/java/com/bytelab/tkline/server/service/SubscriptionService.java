package com.bytelab.tkline.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionQueryDTO;

/**
 * 订阅服务接口
 */
public interface SubscriptionService {

    /**
     * 创建订阅
     *
     * @param createDTO 创建请求
     * @return 订阅ID
     */
    Long createSubscription(SubscriptionCreateDTO createDTO);

    /**
     * 获取订阅详情
     *
     * @param id 订阅ID
     * @return 订阅详情
     */
    SubscriptionDTO getSubscriptionDetail(Long id);

    /**
     * 分页查询订阅
     *
     * @param query 查询条件
     * @return 订阅列表
     */
    IPage<SubscriptionDTO> pageSubscriptions(SubscriptionQueryDTO query);

    /**
     * 更新订阅
     *
     * @param updateDTO 更新请求
     */
    void updateSubscription(com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO updateDTO);

    /**
     * 获取订阅绑定的节点ID列表
     *
     * @param subscriptionId 订阅ID
     * @return 节点ID列表
     */
    java.util.List<Long> getSubscriptionNodeIds(Long subscriptionId);

    /**
     * 分页查询订阅绑定的节点
     *
     * @param subscriptionId 订阅ID
     * @param query          查询条件
     * @return 节点列表
     */
    IPage<NodeDTO> pageSubscriptionNodes(Long subscriptionId, PageQueryDTO query);
}
