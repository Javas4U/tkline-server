# VLESS+Reality 协议完整配置指南

## 目录
1. [协议概述](#协议概述)
2. [基础配置参数详解](#基础配置参数详解)
3. [Reality 配置生成方法](#reality-配置生成方法)
4. [UUID vs Short ID 区别](#uuid-vs-short-id-区别)
5. [协议对比与选择](#协议对比与选择)
6. [实战配置示例](#实战配置示例)
7. [参考资源](#参考资源)

---

## 协议概述

### VLESS 协议
VLESS 是轻量级代理协议，特点：
- **极小头部开销**：相比 VMess 更高效
- **UUID 认证**：基于标准 UUID 的身份验证
- **高性能**：适合高速传输场景

### XTLS Vision 流控
**xtls-rprx-vision** 是最新的流控模式，核心优势：
- ✅ 解决 TLS-in-TLS 问题：避免双重 TLS 加密特征
- ✅ 性能提升：可达数倍性能提升
- ✅ 抗检测：随机填充和指纹模拟
- ✅ 智能识别：跳过不必要的加密层

### REALITY 协议
REALITY 是目前最安全的传输加密方案：
- **完美伪装**：模拟正常网页浏览
- **无服务器特征**：无需真实 TLS 证书
- **前向安全**：即使密钥泄露，历史流量依然安全
- **抗审查**：消除 TLS 指纹特征

---

## 基础配置参数详解

### 完整配置结构

```json
{
    "tag": "节点名-VLESS",
    "type": "vless",
    "server": "1.2.3.4",
    "server_port": 443,
    "uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "flow": "xtls-rprx-vision",
    "tls": {
        "enabled": true,
        "server_name": "www.cloudflare.com",
        "utls": {
            "enabled": true,
            "fingerprint": "chrome"
        },
        "reality": {
            "enabled": true,
            "public_key": "YOUR_REALITY_PUBLIC_KEY",
            "short_id": "a1b2c3d4"
        }
    }
}
```

### 参数说明

#### 1. 基础连接参数

| 参数 | 说明 |
|------|------|
| `tag` | 节点标识符，用于路由规则引用 |
| `type` | 协议类型，固定为 `vless` |
| `server` | 代理服务器地址（IP 或域名） |
| `server_port` | 服务器端口，建议 443（伪装 HTTPS） |
| `uuid` | 客户端身份认证凭证（128-bit） |

#### 2. Flow 流控

```json
"flow": "xtls-rprx-vision"
```

- **作用**：启用 XTLS Vision 流控优化
- **工作原理**：智能识别内层 TLS 流量，避免双重加密
- **性能提升**：相比传统加密可达 3-5 倍提升

#### 3. TLS 配置

##### server_name (SNI)
```json
"server_name": "www.cloudflare.com"
```

**关键作用**：
- 告诉服务器要伪装成哪个网站
- 必须在服务器 `serverNames` 白名单中
- 选择大型、稳定、支持 TLS 1.3 的网站

**推荐选择**：
```
www.cloudflare.com
www.microsoft.com
www.apple.com
www.fastly.com
```

##### uTLS 指纹模拟
```json
"utls": {
    "enabled": true,
    "fingerprint": "chrome"
}
```

**fingerprint 选项**：
- `chrome` - 模拟 Chrome 浏览器（推荐）
- `firefox` - 模拟 Firefox
- `safari` - 模拟 Safari
- `edge` - 模拟 Edge
- `randomized` - 完全随机指纹

**用途**：防火墙无法识别代理工具特征

#### 4. Reality 核心配置

##### public_key - 公钥
```json
"public_key": "SLwxqKq1VoT8E_RMhZp6sGMVLPCzQJvmE9nAZFCZm3M"
```

**作用**：
- 客户端验证服务器身份
- 替代传统证书链验证
- 确保前向安全性

##### short_id - 短 ID
```json
"short_id": "6ba85179e30d4fc2"
```

**格式要求**：
- 最多 16 位十六进制字符
- 长度必须是偶数
- 可以为空字符串 `""`

**用途**：
- 区分不同客户端
- 快速识别合法客户端
- 支持多用户场景

---

## Reality 配置生成方法

### 1. 生成 Public/Private Key

#### 方法 A：使用 Xray 命令行

```bash
# 直接运行
./xray x25519
# 或者
xray x25519

# 输出示例：
# Private key: M4cZLR81ErNfxnG1fAnNUIATs_UXqe6HR78wINhH7RA
# Public key: ioE61VC3V30U7IdRmQ3bjhOq2ij9tPhVIgAD4JZ4YRY
```

#### 方法 B：Docker 环境

```bash
docker run --rm ghcr.io/xtls/xray-core x25519
```

**使用规则**：
- ✅ Private key → 配置在服务器端
- ✅ Public key → 分发给客户端

### 2. 生成 Short ID

#### 方法 A：OpenSSL 随机生成

```bash
# 生成 8 位十六进制（推荐）
openssl rand -hex 4
# 输出: 6ba85179

# 生成 16 位十六进制（最大长度）
openssl rand -hex 8
# 输出: 6ba85179e30d4fc2
```

#### 方法 B：使用 Xray

```bash
# 某些版本支持
xray uuid --short
```

**格式要求**：
- 长度：0-16 个字符
- 字符集：`0-9` 和 `a-f`（十六进制）
- 长度必须是偶数
- 可以为空字符串

---

## UUID vs Short ID 区别

### 对比表格

| 特性 | UUID | Short ID |
|------|------|----------|
| **协议层** | VLESS 协议层认证 | Reality TLS 握手层认证 |
| **格式** | 128-bit 标准 UUID | 0-16 字符十六进制 |
| **用途** | 主要客户端身份标识 | TLS Session ID 嵌入认证 |
| **必需性** | 必须唯一 | 可以为空或相同 |
| **位置** | VLESS 协议头部 | TLS ClientHello 的 Session ID 字段 |

### Short ID 可以设置为相同吗？

**✅ 可以！** 官方说明：
> "If an empty string exists in the shortIds list, client shortId can be empty."
>
> "shortIds can be used to distinguish different clients"

### 三种配置策略

#### 策略 1：所有客户端使用相同 Short ID（推荐）

```json
// 服务器配置
"shortIds": ["6ba85179"]

// 客户端 A, B, C 都使用
"short_id": "6ba85179"
```

✅ **优点**：配置简单，便于管理
⚠️ **缺点**：无法区分不同客户端

#### 策略 2：为每个客户端分配不同 Short ID

```json
// 服务器配置
"shortIds": ["", "6ba85179", "a1b2c3d4", "12345678"]

// 客户端 A
"short_id": "6ba85179"

// 客户端 B
"short_id": "a1b2c3d4"

// 客户端 C（可使用空字符串）
"short_id": ""
```

✅ **优点**：可通过日志区分客户端流量
⚠️ **缺点**：管理复杂度增加

#### 策略 3：使用空字符串（最宽松）

```json
// 服务器配置
"shortIds": [""]

// 所有客户端
"short_id": ""
```

✅ **优点**：最大兼容性
⚠️ **警告**：降低一定安全性

### 推荐做法（订阅系统）

如果已经使用 UUID 来区分客户端，建议 **Short ID 使用统一值**：

```java
// 所有订阅使用相同的 short_id
String shortId = "6ba85179"; // 固定值

// 或者基于订单号生成（可选）
String shortId = subscription.getOrderNo()
    .substring(0, 8)
    .toLowerCase();
```

**理由**：
- UUID 已经提供了客户端识别
- Short ID 主要用于 TLS 握手验证
- 简化配置管理

---

## 协议对比与选择

### 三大协议性能对比

| 维度 | Hysteria2 | VLESS+Reality | Trojan |
|------|-----------|---------------|--------|
| **传输协议** | QUIC (UDP) | TCP | TCP |
| **速度（好网络）** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **速度（差网络）** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **延迟** | 35-50ms | 25-40ms | 30-45ms |
| **抗丢包** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |
| **UDP 支持** | ✅ 原生 | ✅ 支持 | ✅ 支持 |
| **拥塞控制** | BBR 变种 | 标准 TCP | 标准 TCP |
| **被封锁风险** | ⚠️ 高（UDP 特征明显） | ✅ 低（伪装完美） | ✅ 低 |
| **CDN 兼容** | ❌ 不支持 | ✅ 支持 | ✅ 支持 |

### 实测性能数据

| 场景 | Hysteria2 | VLESS+Reality | Trojan |
|------|-----------|---------------|--------|
| **1080p 直播推流** | 39ms 延迟 | 45ms 延迟 | 52ms 延迟 |
| **4K 视频上传** | 28MB/s | 35MB/s | 25MB/s |
| **跨境 API 调用** | 42ms RTT | 38ms RTT | 44ms RTT |
| **被封锁概率** | 高（30%） | 低（5%） | 低（8%） |

### 应用场景推荐

#### 🎬 场景 1：短视频（TikTok/抖音国际版）

**推荐**：VLESS+Reality ⭐⭐⭐⭐⭐

**理由**：
- ✅ 短视频上传需要稳定的 TCP 连接
- ✅ Reality 伪装成 Cloudflare 流量，不易被检测
- ✅ XTLS Vision 流控优化，上传速度快
- ⚠️ Hysteria2 的 UDP 流量可能被运营商 QoS 限速

**最佳配置**：
```json
{
  "type": "vless",
  "flow": "xtls-rprx-vision",
  "server_name": "www.cloudflare.com"
}
```

#### 📺 场景 2：跨境直播（Facebook/Instagram Live）

**推荐**：
- Hysteria2 ⭐⭐⭐⭐⭐（网络质量差时）
- VLESS+Reality ⭐⭐⭐⭐（网络质量好时）

**Hysteria2 优势**：
- ✅ 实测延迟 <50ms
- ✅ 双向拥塞控制，适合实时交互
- ✅ 抗丢包能力强，移动网络稳定
- ✅ QUIC 协议天然适合流媒体

**VLESS+Reality 优势**：
- ✅ TCP 更稳定，不会被 ISP 限制 UDP
- ✅ 伪装性好，长期使用不易被封
- ✅ 配合 BBR 拥塞算法也能达到低延迟

**推荐策略**：
```yaml
# 主节点：Hysteria2（高速推流）
- name: "Live-HY2"
  type: hysteria2
  up: 50      # 上行 50Mbps（1080p 直播足够）
  down: 100   # 下行 100Mbps

# 备用节点：VLESS+Reality（稳定性优先）
- name: "Live-VLESS-Backup"
  type: vless
  flow: xtls-rprx-vision
```

#### 🛒 场景 3：跨境电商（Shopify/Amazon Seller）

**推荐**：VLESS+Reality ⭐⭐⭐⭐⭐

**理由**：
- ✅ 需要长时间稳定连接
- ✅ Reality 伪装性最强，避免账号异常
- ✅ TCP 更适合 HTTP/HTTPS 请求
- ✅ 支持 CDN 加速（Cloudflare Workers）

#### 💬 场景 4：实时客服/视频会议（Zoom/Teams）

**推荐**：Trojan ⭐⭐⭐⭐⭐

**理由**：
- ✅ HTTPS 伪装，穿透性强
- ✅ UDP 支持良好（语音/视频通话）
- ✅ 兼容性最好，几乎所有客户端支持
- ✅ 配置简单，稳定性高

### 综合推荐方案

#### 最佳实践：多协议智能切换

```yaml
# 1. 主力节点：VLESS+Reality（日常使用）
- name: "Main-VLESS"
  type: vless
  flow: xtls-rprx-vision
  reality-opts:
    public-key: <公钥>
    short-id: "6ba85179"

# 2. 高速节点：Hysteria2（直播/大文件）
- name: "Fast-HY2"
  type: hysteria2
  up: 100
  down: 500

# 3. 备用节点：Trojan（稳定性）
- name: "Backup-Trojan"
  type: trojan
  udp: true
```

#### 智能路由规则

```yaml
rules:
  # 直播平台 → Hysteria2
  - DOMAIN-KEYWORD,facebook,Fast-HY2
  - DOMAIN-KEYWORD,instagram,Fast-HY2
  - DOMAIN-KEYWORD,tiktok,Fast-HY2

  # 电商平台 → VLESS+Reality
  - DOMAIN-KEYWORD,shopify,Main-VLESS
  - DOMAIN-KEYWORD,amazon,Main-VLESS

  # 其他流量 → 自动选择
  - MATCH,PROXY
```

### 最终建议

| 用途 | 首选协议 | 备选协议 |
|------|---------|---------|
| **短视频 + 电商运营** | VLESS+Reality | Trojan |
| **实时直播（核心业务）** | Hysteria2（网络差）/ VLESS+Reality（网络好） | Trojan |
| **长期稳定性** | VLESS+Reality > Trojan > Hysteria2 | - |

---

## 实战配置示例

### Sing-Box 格式（推荐）

```json
{
    "tag": "HK-Node-VLESS",
    "type": "vless",
    "server": "proxy.example.com",
    "server_port": 443,
    "uuid": "12345678-1234-5678-1234-567812345678",

    // XTLS Vision 流控
    "flow": "xtls-rprx-vision",

    "tls": {
        "enabled": true,

        // 伪装目标网站
        "server_name": "www.cloudflare.com",

        // 浏览器指纹模拟
        "utls": {
            "enabled": true,
            "fingerprint": "chrome"
        },

        // Reality 反审查技术
        "reality": {
            "enabled": true,
            "public_key": "SLwxqKq1VoT8E_RMhZp6sGMVLPCzQJvmE9nAZFCZm3M",
            "short_id": "6ba85179e30d4fc2"
        }
    }
}
```

### Clash Meta 格式

```yaml
- name: "HK-Node-VLESS"
  type: vless
  server: proxy.example.com
  port: 443
  uuid: 12345678-1234-5678-1234-567812345678
  network: tcp
  tls: true
  udp: true

  # XTLS Vision 流控
  flow: xtls-rprx-vision

  # SNI 伪装
  servername: www.cloudflare.com

  # Reality 配置
  reality-opts:
    public-key: SLwxqKq1VoT8E_RMhZp6sGMVLPCzQJvmE9nAZFCZm3M
    short-id: 6ba85179e30d4fc2

  # 浏览器指纹
  client-fingerprint: chrome
```

### URL 分享格式

```
vless://{uuid}@{ip}:{port}?flow=xtls-rprx-vision&encryption=none&type=tcp&security=reality&sni={domain}&fp={fingerprint}&pbk={public_key}&sid={shortid}&spx={spiderx}#{name}
```

**示例**：
```
vless://12345678-1234-5678-1234-567812345678@proxy.example.com:443?flow=xtls-rprx-vision&encryption=none&type=tcp&security=reality&sni=www.cloudflare.com&fp=chrome&pbk=SLwxqKq1VoT8E_RMhZp6sGMVLPCzQJvmE9nAZFCZm3M&sid=6ba85179e30d4fc2#HK-Node-VLESS
```

### Java 订阅系统实现

```java
// 根据节点类型自动选择协议
if (node.getPort() == 443) {
    // 443 端口优先用 VLESS+Reality（伪装 HTTPS）
    protocols = List.of("vless", "reality");
} else if (node.getPort() >= 20000 && node.getPort() <= 30000) {
    // 高端口用 Hysteria2（高速）
    protocols = List.of("hy2");
} else {
    // 其他端口用 Trojan（兼容性）
    protocols = List.of("trojan");
}

// UUID 生成（基于订单号）
UUID uuid = UUID.nameUUIDFromBytes(
    subscription.getOrderNo().getBytes()
);

// Short ID 生成（统一值或基于订单号）
String shortId = "6ba85179"; // 固定值，推荐
// 或者：
// String shortId = subscription.getOrderNo()
//     .substring(0, 8).toLowerCase();
```

---

## 安全性总结

### VLESS+Reality 的三层防护

1. **协议层**：VLESS 轻量高效，减少特征
2. **传输层**：XTLS Vision 优化性能，解决 TLS-in-TLS
3. **伪装层**：Reality + uTLS 完美模拟真实 HTTPS 流量

### 为什么难以被检测？

✅ **无服务器特征**：不需要真实证书，无法通过证书链追踪
✅ **流量伪装**：完全模拟访问 Cloudflare 等大型网站
✅ **指纹模拟**：TLS 握手与真实 Chrome 浏览器一致
✅ **前向安全**：即使密钥泄露，历史流量依然安全

---

## 注意事项

### ⚠️ 必须匹配的参数

| 参数 | 要求 |
|------|------|
| `uuid` | 客户端与服务器必须相同 |
| `public_key` | 与服务器的 `private_key` 对应 |
| `short_id` | 必须在服务器的 `shortIds` 列表中 |
| `server_name` | 必须在服务器的 `serverNames` 列表中 |

### ⚠️ 建议的 server_name 选择

**大型 CDN**：
- `www.cloudflare.com`
- `www.fastly.com`

**科技公司**：
- `www.microsoft.com`
- `www.apple.com`

**要求**：
- 支持 TLS 1.3
- 在本地可访问
- 流量大且稳定

---

## 参考资源

### 官方文档
- [Xray REALITY Protocol Official Documentation](https://github.com/XTLS/REALITY/blob/main/README.en.md)
- [VLESS Protocol Configuration Guide](https://xtls.github.io/en/config/outbounds/vless.html)
- [Xray Command Parameters - X25519 Key Generation](https://xtls.github.io/en/document/command.html)

### 技术文章
- [Config VLESS with Advanced Features in Sing-Box](https://www.jamesflare.com/vless-tcp-reality-xtls-utls-xudp/)
- [Xray with Reality + Vision + uTLS Guide](https://j3ffyang.medium.com/xray-with-reality-vision-utls-3abfb63b682e)
- [REALITY Protocol Deep Dive](https://deepwiki.com/XTLS/Xray-core/3.2-reality-protocol)

### 性能对比
- [Comparison of Different Protocols - XTLS/Xray-core Discussion](https://github.com/XTLS/Xray-core/discussions/2950)
- [Hysteria2 & VLESS-gRPC-uTLS-REALITY Performance Comparison](https://geekbb.xlog.page/Hysteria2--VLESS-gRPC-uTLS-REALITY-dui-bi-ce-shi?locale=en)
- [Protocol Selection Guide - WannaFlix](https://docs.wannaflix.net/which-protocol-to-choose)

### 跨境业务优化
- [Live E-commerce Network Latency Optimization (<50ms)](https://www.ipipgo.com/en-us/ipdaili/18221.html)
- [Circumvention Software and Protocols: A Practical Guide](https://atlassc.net/2025/11/12/circumvention-software-and-protocols)

---

## 版本信息

- **文档版本**：v1.0
- **最后更新**：2026-01-13
- **适用协议版本**：Xray-core 1.8+, Sing-Box 1.8+

---

## 附录：常见问题

### Q1: 如何验证配置是否正确？
```bash
# 测试服务器配置
xray run -test -config config.json

# 查看实时日志
xray run -config config.json
```

### Q2: 如何优化延迟？
- 选择地理位置近的服务器
- 启用 BBR 拥塞控制
- 使用 XTLS Vision 流控
- 选择骨干网络运营商（NTT/Level3）

### Q3: 如何处理连接失败？
1. 检查 `uuid` 是否匹配
2. 验证 `public_key` 是否正确
3. 确认 `short_id` 在服务器白名单中
4. 测试 `server_name` 是否可访问

### Q4: 如何监控节点状态？
```bash
# 检查连接延迟
ping {server_ip}

# 测试端口连通性
telnet {server_ip} {port}

# 测试 TLS 握手
openssl s_client -connect {server_ip}:{port} -servername {server_name}
```

---

**文档维护者**：tkline-server 项目组
**技术支持**：请提交 Issue 或 Pull Request
