package com.bytelab.tkline.server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 生成邀请码请求DTO
 * <p>
 * Feature: 004-invitation-referral
 * User Story: US2 - 生成和管理邀请码
 * 
 * @author AI Assistant
 * @date 2025-10-24
 */
@Data
@Schema(description = "生成邀请码请求(无参数,使用SecurityUtils获取userId)")
public class GenerateInviteCodeDTO {
    // 无参数,使用SecurityUtils获取userId
}

