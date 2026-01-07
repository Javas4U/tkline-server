package com.bytelab.tkline.server.controller;

import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.security.*;
import com.bytelab.tkline.server.service.core.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 安全密钥管理控制器
 * 处理密钥管理相关的HTTP请求，包括密钥的查询、轮换和设置活跃密钥等操作
 */
@Slf4j
@RestController
@RequestMapping("/api/security/keys")
@RequiredArgsConstructor
@Tag(name = "安全密钥管理", description = "RSA密钥对管理、加密解密等安全相关接口")
public class SecurityController {

    private final SecurityService securityService;

    /**
     * 获取当前活跃的公钥
     * 前端调用此接口获取公钥，用于加密敏感信息（如登录密码）
     *
     * @return 公钥信息
     */
    @GetMapping("/public-key")
    @Operation(summary = "获取活跃公钥", description = "获取当前活跃的RSA公钥，前端使用该公钥加密敏感数据")
    public ApiResult<PublicKeyDTO> getActivePublicKey() {
        log.info("请求获取活跃公钥");
        PublicKeyDTO publicKey = securityService.getActivePublicKey();
        return ApiResult.success(publicKey);
    }
}

