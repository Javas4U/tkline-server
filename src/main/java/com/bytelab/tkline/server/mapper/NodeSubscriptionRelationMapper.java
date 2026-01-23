package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytelab.tkline.server.entity.NodeSubscriptionRelation;
import com.bytelab.tkline.server.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NodeSubscriptionRelationMapper extends BaseMapper<NodeSubscriptionRelation> {
    /**
     * 批量插入
     */
    int insertBatch(@Param("list") List<NodeSubscriptionRelation> list);

    /**
     * 批量逻辑删除
     */
    int deleteBatch(@Param("idList") List<Long> idList);

    /**
     * 查询节点关联的所有订阅
     */
    @Select("SELECT s.* FROM subscription s " +
            "INNER JOIN node_subscription_relation nsr ON s.id = nsr.subscription_id " +
            "WHERE nsr.node_id = #{nodeId} AND nsr.deleted = 0 AND s.deleted = 0")
    List<Subscription> selectSubscriptionsByNodeIdList(@Param("nodeId") Long nodeId);
}
