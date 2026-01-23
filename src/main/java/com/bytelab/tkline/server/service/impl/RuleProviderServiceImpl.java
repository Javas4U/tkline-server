package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.converter.RuleProviderConverter;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderCreateDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderQueryDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderUpdateDTO;
import com.bytelab.tkline.server.entity.RuleProvider;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.RuleProviderMapper;
import com.bytelab.tkline.server.service.RuleProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Rule Provider Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleProviderServiceImpl extends ServiceImpl<RuleProviderMapper, RuleProvider>
        implements RuleProviderService {

    private final RuleProviderConverter ruleProviderConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRuleProvider(RuleProviderCreateDTO createDTO) {
        // 检查名称是否已存在
        boolean exists = this.exists(new LambdaQueryWrapper<RuleProvider>()
                .eq(RuleProvider::getName, createDTO.getName()));
        if (exists) {
            throw new BusinessException("规则提供者名称已存在: " + createDTO.getName());
        }

        RuleProvider ruleProvider = ruleProviderConverter.toEntity(createDTO);
        this.save(ruleProvider);
        log.info("创建规则提供者成功: id={}, name={}", ruleProvider.getId(), ruleProvider.getName());
        return ruleProvider.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRuleProvider(RuleProviderUpdateDTO updateDTO) {
        RuleProvider existing = this.getById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException("规则提供者不存在: " + updateDTO.getId());
        }

        // 检查名称是否与其他记录冲突
        boolean nameExists = this.exists(new LambdaQueryWrapper<RuleProvider>()
                .eq(RuleProvider::getName, updateDTO.getName())
                .ne(RuleProvider::getId, updateDTO.getId()));
        if (nameExists) {
            throw new BusinessException("规则提供者名称已存在: " + updateDTO.getName());
        }

        ruleProviderConverter.updateEntityFromDto(updateDTO, existing);
        boolean success = this.updateById(existing);
        if (!success) {
            throw new BusinessException("更新失败，可能已被其他用户修改");
        }
        log.info("更新规则提供者成功: id={}, name={}", existing.getId(), existing.getName());
    }

    @Override
    public RuleProviderDTO getRuleProviderDetail(Long id) {
        RuleProvider ruleProvider = this.getById(id);
        if (ruleProvider == null) {
            throw new BusinessException("规则提供者不存在: " + id);
        }
        return ruleProviderConverter.toDTO(ruleProvider);
    }

    @Override
    public IPage<RuleProviderDTO> pageRuleProviders(RuleProviderQueryDTO query) {
        Page<RuleProvider> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<RuleProvider> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getName())) {
            wrapper.like(RuleProvider::getName, query.getName());
        }
        if (StringUtils.isNotBlank(query.getType())) {
            wrapper.eq(RuleProvider::getType, query.getType());
        }
        if (StringUtils.isNotBlank(query.getBehavior())) {
            wrapper.eq(RuleProvider::getBehavior, query.getBehavior());
        }
        if (query.getStatus() != null) {
            wrapper.eq(RuleProvider::getStatus, query.getStatus());
        }

        // 按排序字段和ID排序
        wrapper.orderByAsc(RuleProvider::getSortOrder)
                .orderByDesc(RuleProvider::getId);

        IPage<RuleProvider> result = this.page(page, wrapper);
        return result.convert(ruleProviderConverter::toDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRuleProvider(Long id) {
        RuleProvider ruleProvider = this.getById(id);
        if (ruleProvider == null) {
            throw new BusinessException("规则提供者不存在: " + id);
        }
        this.removeById(id);
        log.info("删除规则提供者成功: id={}, name={}", id, ruleProvider.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRuleProviders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        this.removeByIds(ids);
        log.info("批量删除规则提供者成功: count={}", ids.size());
    }

    @Override
    public List<RuleProvider> getEnabledRuleProviders() {
        return this.list(new LambdaQueryWrapper<RuleProvider>()
                .eq(RuleProvider::getStatus, 1)
                .orderByAsc(RuleProvider::getSortOrder)
                .orderByDesc(RuleProvider::getId));
    }
}
