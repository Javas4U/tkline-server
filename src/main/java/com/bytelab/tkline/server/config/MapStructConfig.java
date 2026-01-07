package com.bytelab.tkline.server.config;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct 全局配置
 *
 * <p>
 * 功能：统一配置所有 MapStruct Mapper 的行为和策略
 *
 * <p>
 * 配置说明：
 * - componentModel: "spring" - 使用 Spring 的依赖注入
 * - injectionStrategy: CONSTRUCTOR - 构造函数注入（推荐，便于测试）
 * - unmappedTargetPolicy: IGNORE - 忽略未映射的字段（避免噪音警告）
 * - typeConversionPolicy: REPORT_ERROR - 类型转换错误时报告错误
 * - mappingInheritanceStrategy: AUTO_INHERIT_ALL_FROM_CONFIG - 自动继承配置
 *
 * <p>
 * 使用方式：在所有 Mapper 接口上添加 @MapperConfig 注解引用此配置
 * @author Apex Team
 * @since 2025-11-12
 */
@MapperConfig(
    // 使用 Spring 的组件模型（自动注册为 Spring Bean）
    componentModel = MappingConstants.ComponentModel.SPRING,

    // 使用构造函数注入（更安全，便于单元测试）
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,

    // 忽略未映射的目标字段（避免大量警告信息）
    unmappedTargetPolicy = ReportingPolicy.IGNORE,

    // 当源类型和目标类型不匹配时的处理策略
    typeConversionPolicy = ReportingPolicy.ERROR,

    // 映射继承策略（从父接口继承配置）
    mappingInheritanceStrategy = org.mapstruct.MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG
)
public interface MapStructConfig {

    /**
     * 示例：全局日期格式转换
     * <p>
     * 注意：实际项目中，日期转换通常使用注解 @Mapping 单独配置
     * 这里仅作示例展示
     */
    // @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    // @Mapping(target = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")

}
