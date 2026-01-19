package com.bytelab.tkline.server.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用操作响应
 * 
 * 适用于简单的操作结果响应（如删除、更新等）
 * 仅包含success和message字段
 * 
 * 注意：此类不继承BaseResponse，避免SuperBuilder问题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用操作响应")
public class CommonResponse {
    
    @Schema(description = "是否成功", example = "true")
    private Boolean success;
    
    @Schema(description = "提示消息", example = "操作成功")
    private String message;
}

