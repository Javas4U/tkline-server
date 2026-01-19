package com.bytelab.tkline.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Reality 协议配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "reality")
public class RealityConfig {

    /**
     * Reality 公钥（用于客户端配置）
     */
    private String publicKey;

    /**
     * Reality 私钥（用于服务端配置，请勿公开）
     */
    private String privateKey;

    /**
     * Reality Short ID
     */
    private String shortId;
}
