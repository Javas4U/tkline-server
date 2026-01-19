package com.bytelab.tkline.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 响应基类
 * 
 * 包含所有响应的通用字段：
 * - success: 操作是否成功
 * - message: 提示消息
 * 
 * 所有XxxResponse都应该继承此类
 * 
 * 注意：
 * - 使用@SuperBuilder支持继承的Builder模式
 * - 子类也需要使用@SuperBuilder
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "响应基类")
public abstract class BaseResponse {
    
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;
    
    @Schema(description = "提示消息", example = "操作成功")
    private String message;
}

