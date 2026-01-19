package com.bytelab.tkline.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置邀请码请求DTO
 * <p>
 * Feature: 004-invitation-referral
 * User Story: US2 - 生成和管理邀请码
 * 
 * @author AI Assistant
 * @date 2025-10-24
 */
@Data
@Schema(description = "重置邀请码请求")
public class ResetInviteCodeDTO {
    
    @NotBlank(message = "旧邀请码不能为空")
    @Schema(description = "旧邀请码", example = "A1B2C3D4", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldCode;
    
    // 无需新邀请码参数,系统自动生成
}

