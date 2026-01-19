package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytelab.tkline.server.entity.NodeSubscriptionRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
