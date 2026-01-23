# 配置更新器 API 文档

## 概述

此API用于为 singbox-config-updater 服务提供用户数据,支持动态更新代理服务器配置。

## API 端点

### GET /api/subscription/users

获取所有订阅用户信息,用于配置更新器同步用户。

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 1000 | 每页数量 |

#### 请求示例

```bash
GET /api/subscription/users?page=1&pageSize=1000
```

#### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "name": "VIP用户组",
        "uuid": "ORD20240120123456",
        "password": "ORD20240120123456",
        "protocol": "vless"
      },
      {
        "name": "VIP用户组",
        "uuid": "ORD20240120123456",
        "password": "ORD20240120123456",
        "protocol": "hysteria2"
      },
      {
        "name": "普通用户组",
        "uuid": "ORD20240120123457",
        "password": "ORD20240120123457",
        "protocol": "trojan"
      }
    ],
    "total": 5000,
    "size": 1000,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1705737600000
}
```

#### 响应字段说明

| 字段名 | 类型 | 描述 |
|--------|------|------|
| name | String | 用户组名称(subscription.group_name) |
| uuid | String | 订单号(node_subscription_relation.order_no),用于 VLESS 和 TUIC |
| password | String | 订单号(node_subscription_relation.order_no),用于 Hysteria2 和 Trojan |
| protocol | String | 协议类型: hysteria2, vless, trojan, tuic |

## 数据来源

API 从以下数据源获取信息:

1. **node_subscription_relation** 表 - 订阅与节点的关联关系
   - `order_no`: 订单号,用作 uuid 和 password
   - `status = 1`: 只返回有效的关联关系

2. **subscription** 表 - 订阅基本信息
   - `group_name`: 用户组名称

3. **node** 表 - 节点信息
   - `protocols`: 节点支持的协议列表(逗号分隔)

## 协议展开逻辑

每个订阅-节点关联会根据节点支持的协议数量展开为多条记录。

**示例**:
- 订阅 "VIP用户组" (orderNo: ORD001)
- 绑定到节点 "香港节点" (protocols: "vless,hysteria2,trojan")
- 展开为 3 条记录:
  1. { name: "VIP用户组", uuid: "ORD001", password: "ORD001", protocol: "vless" }
  2. { name: "VIP用户组", uuid: "ORD001", password: "ORD001", protocol: "hysteria2" }
  3. { name: "VIP用户组", uuid: "ORD001", password: "ORD001", protocol: "trojan" }

## 配置更新器集成

### 环境变量配置

在 singbox-config-updater 的 `.env` 文件中配置:

```env
API_BASE_URL=http://your-tkline-server:port
API_KEY=your-api-key-if-needed
```

### 使用说明

1. 配置更新器会定时调用此 API 获取最新的用户列表
2. 根据 protocol 字段自动分组用户到对应的 inbound
3. 只更新配置文件中的 users 数组,保持其他配置不变
4. 检测到配置变化后自动重启代理服务容器

## 注意事项

1. **分页处理**: API 使用分页返回数据,配置更新器需要循环获取所有页
2. **协议名称**: 协议名称统一使用小写 (hysteria2, vless, trojan, tuic)
3. **数据一致性**: uuid 和 password 字段都使用订单号,确保数据一致性
4. **性能优化**:
   - 使用批量查询优化数据库访问
   - 默认 pageSize 为 1000,可根据实际情况调整
5. **错误处理**:
   - 订阅或节点不存在时记录警告并跳过
   - 节点未配置协议时记录警告并跳过

## 测试

### 使用 curl 测试

```bash
curl -X GET "http://localhost:8080/api/subscription/users?page=1&pageSize=10"
```

### 预期响应

返回包含用户列表的分页数据,每个用户包含 name、uuid、password 和 protocol 字段。

## 版本历史

### v1.0.0 (2024-01-20)
- 初始版本
- 支持分页查询订阅用户信息
- 支持多协议展开 (Hysteria2, VLESS, Trojan, TUIC)
- 基于订阅-节点关联关系动态生成用户列表
