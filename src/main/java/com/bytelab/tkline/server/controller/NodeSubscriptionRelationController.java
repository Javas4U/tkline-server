package com.bytelab.tkline.server.controller;

import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.relation.RelationBindDTO;
import com.bytelab.tkline.server.service.NodeSubscriptionRelationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 节点订阅关联控制器
 */
@RestController
@RequestMapping("/api/v1/relation")
@RequiredArgsConstructor
public class NodeSubscriptionRelationController {

    private final NodeSubscriptionRelationService relationService;

    /**
     * 绑定节点和订阅
     */
    @PostMapping("/bindNodeSubscriptions")
    public ApiResult<Void> bindNodeSubscriptions(@RequestBody @Valid RelationBindDTO bindDTO) {
        relationService.bindNodeSubscriptions(bindDTO);
        return ApiResult.success();
    }
}
