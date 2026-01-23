package com.bytelab.tkline.server.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 代理用户DTO
 * <p>
 * 用于配置更新器获取用户信息
 */
@Data
@Schema(description = "代理用户信息")
public class ProxyUserDTO {

    @Schema(description = "用户名称(group_name)")
    private String name;

    @Schema(description = "UUID(订单号,用于VLESS和TUIC)")
    private String uuid;

    @Schema(description = "密码(订单号,用于Hysteria2和Trojan)")
    private String password;

    @Schema(description = "协议类型: hysteria2, vless, trojan, tuic")
    private String protocol;
}
