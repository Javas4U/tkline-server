package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderCreateDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderUpdateDTO;
import com.bytelab.tkline.server.entity.RuleProvider;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Rule Provider Converter
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RuleProviderConverter {

    /**
     * Entity 转 DTO
     */
    RuleProviderDTO toDTO(RuleProvider entity);

    /**
     * CreateDTO 转 Entity
     */
    RuleProvider toEntity(RuleProviderCreateDTO createDTO);

    /**
     * 从 UpdateDTO 更新 Entity
     */
    void updateEntityFromDto(RuleProviderUpdateDTO updateDTO, @MappingTarget RuleProvider entity);
}
