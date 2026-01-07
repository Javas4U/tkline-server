package com.bytelab.tkline.server.service;

import com.bytelab.tkline.server.dto.relation.RelationBindDTO;

import java.util.List;

/**
 * 节点订阅关联服务接口
 */
public interface NodeSubscriptionRelationService {

    /**
     * 绑定节点和订阅
     *
     * @param bindDTO 绑定请求
     */
    void bindNodeSubscriptions(RelationBindDTO bindDTO);

    /**
     * 同步订阅绑定的节点（更新时使用：删除不在列表中的，新增不在数据库中的）
     *
     * @param subscriptionId 订阅ID
     * @param nodeIds        节点ID列表
     */
    void syncSubscriptionNodes(Long subscriptionId, List<Long> nodeIds);
}
