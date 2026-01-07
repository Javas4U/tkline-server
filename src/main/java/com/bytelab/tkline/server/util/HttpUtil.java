package com.bytelab.apex.tunnel.server.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 工具类
 * 用于解析请求信息（IP、User-Agent等）
 */
@Slf4j
public class HttpUtil {

    /**
     * 获取客户端真实IP
     * 支持各种代理和负载均衡场景
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多次反向代理后会有多个IP值，第一个IP为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip.trim();
        }

        ip = request.getRemoteAddr();
        return ip == null ? "unknown" : ip.trim();
    }

    /**
     * 验证IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 从 User-Agent 中解析浏览器信息
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("edg")) {
            return "Edge";
        } else if (userAgent.contains("chrome")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "IE";
        } else {
            return "Other";
        }
    }

    /**
     * 从 User-Agent 中解析操作系统信息
     */
    public static String getOperatingSystem(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows nt 10.0")) {
            return "Windows 10";
        } else if (userAgent.contains("windows nt 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("windows nt 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("windows nt 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac os x")) {
            return "macOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else {
            return "Other";
        }
    }

    /**
     * 从 User-Agent 中解析设备类型
     */
    public static String getDeviceType(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || 
            userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "PC";
        }
    }

    /**
     * 生成设备唯一标识（基于User-Agent和其他信息）
     */
    public static String generateDeviceId(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = getClientIp(request);

        // 使用 User-Agent + IP 的哈希值作为设备ID
        String rawDeviceId = userAgent + "|" + ip;
        return String.valueOf(rawDeviceId.hashCode());
    }

    /**
     * 获取客户端真实协议(支持反向代理)
     * 优先从 X-Forwarded-Proto 或 X-Forwarded-Ssl 头部获取
     *
     * @param request HTTP请求
     * @return 协议类型 (http 或 https)
     */
    public static String getRealScheme(HttpServletRequest request) {
        if (request == null) {
            return "http";
        }

        // 1. 检查 X-Forwarded-Proto (标准头部,由 Nginx/Apache 等设置)
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto != null && !proto.isEmpty()) {
            // 多次代理可能有多个值,取第一个
            int index = proto.indexOf(',');
            if (index != -1) {
                proto = proto.substring(0, index);
            }
            return proto.trim().toLowerCase();
        }

        // 2. 检查 X-Forwarded-Ssl (某些代理使用)
        String ssl = request.getHeader("X-Forwarded-Ssl");
        if ("on".equalsIgnoreCase(ssl)) {
            return "https";
        }

        // 3. 检查 Front-End-Https (某些负载均衡器使用)
        String frontEndHttps = request.getHeader("Front-End-Https");
        if ("on".equalsIgnoreCase(frontEndHttps)) {
            return "https";
        }

        // 4. 回退到 request.getScheme()
        return request.getScheme();
    }

    /**
     * 构建服务器基础URL(支持反向代理)
     * 自动识别真实协议、域名和端口
     *
     * @param request HTTP请求
     * @return 基础URL,例如: https://example.com 或 http://localhost:8080
     */
    public static String getBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return "http://localhost";
        }

        // 获取真实协议
        String scheme = getRealScheme(request);

        // 获取服务器名称(支持 X-Forwarded-Host)
        String serverName = request.getHeader("X-Forwarded-Host");
        if (serverName == null || serverName.isEmpty()) {
            serverName = request.getServerName();
        } else {
            // 多次代理可能有多个值,取第一个
            int index = serverName.indexOf(',');
            if (index != -1) {
                serverName = serverName.substring(0, index);
            }
            serverName = serverName.trim();
        }

        // 获取端口(支持 X-Forwarded-Port)
        int port;
        String forwardedPort = request.getHeader("X-Forwarded-Port");
        if (forwardedPort != null && !forwardedPort.isEmpty()) {
            try {
                // 多次代理可能有多个值,取第一个
                int index = forwardedPort.indexOf(',');
                if (index != -1) {
                    forwardedPort = forwardedPort.substring(0, index);
                }
                port = Integer.parseInt(forwardedPort.trim());
            } catch (NumberFormatException e) {
                port = request.getServerPort();
            }
        } else {
            port = request.getServerPort();
        }

        // 构建 baseUrl
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 标准端口不需要显示
        boolean isStandardPort = (scheme.equals("http") && port == 80)
                              || (scheme.equals("https") && port == 443);
        if (!isStandardPort) {
            baseUrl.append(":").append(port);
        }

        return baseUrl.toString();
    }
}

