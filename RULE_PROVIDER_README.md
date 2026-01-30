# Rule Provider 动态管理功能说明

## 概述

将原本硬编码在 `SubscriptionServiceImpl.java` 中的 `rule-providers` 配置改为可以在前端页面动态管理的功能。规则数据存储在数据库中，支持 CRUD 操作。

## 功能特性

1. **数据库存储**: 规则配置存储在 `rule_provider` 表中
2. **完整的 CRUD 操作**: 支持创建、读取、更新、删除规则
3. **动态加载**: 生成 Clash 配置时从数据库动态加载启用的规则
4. **排序支持**: 通过 `sort_order` 字段控制规则在配置中的顺序
5. **启用/禁用**: 支持启用或禁用特定规则

## 数据库设计

### rule_provider 表结构

```sql
CREATE TABLE `rule_provider` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称/标识符',
    `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型 (http/file)',
    `rule_behavior` VARCHAR(20) NOT NULL COMMENT '行为类型 (domain/ipcidr/classical)',
    `rule_format` VARCHAR(20) NOT NULL COMMENT '格式类型 (mrs/yaml/text)',
    `rule_url` VARCHAR(500) NOT NULL COMMENT '规则源URL',
    `rule_path` VARCHAR(200) NOT NULL COMMENT '本地存储路径',
    `update_interval` INT NOT NULL DEFAULT 86400 COMMENT '更新间隔(秒)',
    `rule_desc` VARCHAR(500) DEFAULT NULL COMMENT '描述信息',
    `rule_policy` VARCHAR(20) NOT NULL DEFAULT 'PROXY' COMMENT '规则策略 (PROXY/DIRECT/REJECT)',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 (0-禁用, 1-启用)',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_name` (`rule_name`, `deleted`)
)
```

**注意**:
- 字段名使用了 `rule_` 前缀以避免与 MySQL 保留字冲突（如 `type`, `format`, `interval`, `path`, `status` 等）。
- `rule_policy` 字段用于指定该规则的策略，支持三种值：
  - `PROXY`: 流量走代理
  - `DIRECT`: 流量直连
  - `REJECT`: 拒绝访问（通常用于广告拦截）

## API 接口

### 1. 创建规则提供者
- **路径**: `POST /api/rule-provider/create`
- **请求体**:
```json
{
  "name": "geosite-example",
  "type": "http",
  "behavior": "domain",
  "format": "mrs",
  "url": "https://example.com/rules.mrs",
  "path": "./ruleset/example.mrs",
  "interval": 86400,
  "description": "示例规则",
  "policy": "PROXY",
  "status": 1,
  "sortOrder": 100
}
```

### 2. 更新规则提供者
- **路径**: `PUT /api/rule-provider/update`
- **请求体**: 同创建接口,但需要包含 `id` 字段

### 3. 获取规则详情
- **路径**: `GET /api/rule-provider/detail/{id}`

### 4. 分页查询规则
- **路径**: `POST /api/rule-provider/page`
- **请求体**:
```json
{
  "page": 1,
  "pageSize": 10,
  "name": "geosite",
  "type": "http",
  "behavior": "domain",
  "status": 1
}
```

### 5. 删除规则提供者
- **路径**: `DELETE /api/rule-provider/delete/{id}`

### 6. 批量删除规则
- **路径**: `DELETE /api/rule-provider/batch-delete`
- **请求体**: `[1, 2, 3]`

### 7. 获取所有启用的规则
- **路径**: `GET /api/rule-provider/enabled`

## 项目结构

```
tkline-server/
├── src/main/java/com/bytelab/tkline/server/
│   ├── entity/
│   │   └── RuleProvider.java              # 实体类
│   ├── dto/ruleprovider/
│   │   ├── RuleProviderDTO.java           # 数据传输对象
│   │   ├── RuleProviderCreateDTO.java     # 创建 DTO
│   │   ├── RuleProviderUpdateDTO.java     # 更新 DTO
│   │   └── RuleProviderQueryDTO.java      # 查询 DTO
│   ├── mapper/
│   │   └── RuleProviderMapper.java        # MyBatis Mapper
│   ├── service/
│   │   ├── RuleProviderService.java       # Service 接口
│   │   └── impl/
│   │       ├── RuleProviderServiceImpl.java    # Service 实现
│   │       └── SubscriptionServiceImpl.java    # 修改后的订阅服务
│   ├── controller/
│   │   └── RuleProviderController.java    # 控制器
│   └── converter/
│       └── RuleProviderConverter.java     # 对象转换器
└── src/main/resources/db/migration/
    └── V1.0.1__create_rule_provider_table.sql  # 数据库迁移脚本
```

## 使用说明

### 1. 数据库迁移
运行迁移脚本创建表并插入默认数据:
```sql
source V1.0.1__create_rule_provider_table.sql
```

### 2. 前端集成
前端需要创建规则管理页面,调用上述 API 进行 CRUD 操作。

### 3. 配置生成
当生成 Clash 配置时,系统会:
1. 从数据库加载所有 `enabled=1` 的规则
2. 按 `sort_order` 排序
3. 生成 `rule-providers` 配置段
4. 根据每条规则的 `rule_policy` 字段，自动生成对应的 `rules` 规则（如 `- RULE-SET,geosite-tiktok,PROXY`）

## 默认数据

系统会自动插入以下默认规则:
- geosite-category-ads-all (广告拦截, policy=REJECT, sort_order=0)
- geosite-tiktok (policy=PROXY, sort_order=1)
- geosite-youtube (policy=PROXY, sort_order=2)
- geosite-google (policy=PROXY, sort_order=3)
- geosite-instagram (policy=PROXY, sort_order=4)
- geosite-facebook (policy=PROXY, sort_order=5)
- geosite-twitter (policy=PROXY, sort_order=6)
- geosite-netflix (policy=PROXY, sort_order=7)
- geosite-openai (policy=PROXY, sort_order=8)
- geosite-telegram (sort_order=9)
- geosite-spotify (sort_order=10)
- geosite-github (sort_order=11)
- geosite-linkedin (sort_order=12)

## 优势

1. **灵活性**: 无需修改代码即可添加/删除规则
2. **可维护性**: 规则集中管理,易于维护
3. **动态性**: 规则修改立即生效
4. **扩展性**: 易于添加新的规则源和配置项

## 注意事项

1. 规则名称 (`name`) 必须唯一
2. 修改规则后,用户需要重新获取订阅配置才能生效
3. 建议定期备份 `rule_provider` 表数据
