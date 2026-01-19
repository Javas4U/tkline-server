package com.bytelab.tkline.server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和验证 JWT Token
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT 密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token 有效期（秒，从配置文件读取）
     */
    @Value("${jwt.expiration}")
    private long expirationSeconds;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationSeconds * 1000); // 转换为毫秒

        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();

        log.debug("生成Token成功，userId: {}, username: {}, expiresAt: {}, expiration配置: {}秒", 
                userId, username, expirationDate, expirationSeconds);

        return token;
    }

    /**
     * 从 Token 中解析 Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败", e);
            throw new RuntimeException("Token无效或已过期");
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 Token 过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 检查 Token 是否即将过期（剩余时间少于总时长的1/6）
     *
     * @param token JWT Token
     * @return true-即将过期，false-未即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
            // 当剩余时间少于总时长的1/6时认为即将过期
            // 例如：60秒过期时间，剩余10秒时认为即将过期
            long threshold = (expirationSeconds * 1000) / 6;
            return timeUntilExpiration < threshold;
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 刷新 Token
     * 如果原Token有效且即将过期，则生成新Token
     *
     * @param oldToken 旧Token
     * @return 新Token
     */
    public String refreshToken(String oldToken) {
        try {
            Claims claims = parseToken(oldToken);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            
            // 生成新Token
            String newToken = generateToken(userId, username);
            
            log.info("Token刷新成功，userId: {}, username: {}", userId, username);
            
            return newToken;
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            throw new RuntimeException("Token刷新失败：" + e.getMessage());
        }
    }
}

