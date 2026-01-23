package com.bytelab.tkline.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderCreateDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderQueryDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderUpdateDTO;
import com.bytelab.tkline.server.entity.RuleProvider;

import java.util.List;

/**
 * Rule Provider Service 接口
 */
public interface RuleProviderService extends IService<RuleProvider> {

    /**
     * 创建规则提供者
     */
    Long createRuleProvider(RuleProviderCreateDTO createDTO);

    /**
     * 更新规则提供者
     */
    void updateRuleProvider(RuleProviderUpdateDTO updateDTO);

    /**
     * 获取规则提供者详情
     */
    RuleProviderDTO getRuleProviderDetail(Long id);

    /**
     * 分页查询规则提供者
     */
    IPage<RuleProviderDTO> pageRuleProviders(RuleProviderQueryDTO query);

    /**
     * 删除规则提供者
     */
    void deleteRuleProvider(Long id);

    /**
     * 批量删除规则提供者
     */
    void batchDeleteRuleProviders(List<Long> ids);

    /**
     * 获取所有启用的规则提供者(用于生成配置)
     */
    List<RuleProvider> getEnabledRuleProviders();
}
