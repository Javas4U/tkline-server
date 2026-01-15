# TKLine Server

TKLine 后端服务，基于 Spring Boot 3.5.6 开发。

## 技术栈

- **框架**: Spring Boot 3.5.6 + Spring Security 6.x
- **ORM**: MyBatis-Plus 3.5.5
- **认证**: JWT (jjwt 0.12.3)
- **数据库**: MySQL 8.0+
- **工具**: MapStruct 1.6.3、Lombok、SpringDoc OpenAPI 2.7.0

## 功能模块

- ✅ 用户认证（登录、注册、JWT Token）
- ✅ 用户管理（用户信息、修改密码）
- ✅ RSA 加密传输
- ✅ BCrypt 密码散列
- ✅ 邮箱验证码

## 快速开始

### 方式一：Docker Compose 启动（推荐）

使用 Docker Compose 启动应用，连接到已有的 MySQL 数据库。

#### 前置条件

- 已有可访问的 MySQL 8.0+ 数据库
- 已创建数据库：`CREATE DATABASE tkline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

#### 1. 环境变量配置

docker-compose 支持以下环境变量配置（所有变量都有默认值，可按需覆盖）：

| 变量名 | 说明 | 默认值 | 是否必须 |
|--------|------|--------|----------|
| `SERVER_PORT` | 服务端口 | 8081 | 否 |
| `DB_HOST` | 数据库主机地址 | localhost | **是** |
| `DB_PORT` | 数据库端口 | 3306 | 否 |
| `DB_NAME` | 数据库名称 | tkline | 否 |
| `DB_USERNAME` | 数据库用户名 | root | 否 |
| `DB_PASSWORD` | 数据库密码 | 无 | **是** |
| `JWT_SECRET` | JWT 签名密钥 | 默认值 | 生产环境必须修改 |
| `MAIL_HOST` | 邮件服务器地址 | smtpdm.aliyun.com | 否 |
| `MAIL_PORT` | 邮件服务器端口 | 465 | 否 |
| `MAIL_USERNAME` | 邮件账号 | noreply@mail.example.com | 否 |
| `MAIL_PASSWORD` | 邮件密码 | your_mail_password | 否 |
| `MAIL_PROTOCOL` | 邮件协议 | smtp | 否 |
| `MAIL_REPLYTO` | 回复地址 | example@126.com | 否 |
| `SPRINGDOC_ENABLED` | Swagger 文档开关 | true | 否 |
| `LOG_LEVEL_APP` | 应用日志级别 | info | 否 |
| `LOG_LEVEL_MYBATIS` | MyBatis 日志级别 | warn | 否 |

**环境变量使用方式**：

1. **通过命令行直接设置**：
   ```bash
   DB_HOST=192.168.1.100 DB_PASSWORD=mypassword docker-compose up -d
   ```

2. **通过 .env 文件设置**（可选）：
   创建 `.env` 文件并添加配置：
   ```bash
   DB_HOST=192.168.1.100
   DB_PASSWORD=mypassword
   JWT_SECRET=your_custom_jwt_secret
   ```
   然后运行：
   ```bash
   docker-compose up -d
   ```

3. **通过系统环境变量**：
   ```bash
   export DB_HOST=192.168.1.100
   export DB_PASSWORD=mypassword
   docker-compose up -d
   ```

**重要提示**：
- 必须配置正确的数据库连接信息（`DB_HOST`、`DB_PASSWORD` 等）
- 生产环境必须修改 `JWT_SECRET`（使用 `openssl rand -hex 64` 生成新密钥）
- 生产环境建议关闭 Swagger 文档（`SPRINGDOC_ENABLED=false`）

#### 2. 启动服务

```bash
# 启动所有服务（后台运行）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 只查看应用日志
docker-compose logs -f app
```

#### 3. 访问服务

服务启动后访问：
- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

#### 4. 停止和清理

```bash
# 停止服务
docker-compose down

# 停止服务并删除数据卷（会清除所有数据）
docker-compose down -v

# 重启服务
docker-compose restart

# 重启单个服务
docker-compose restart app
```

#### 5. 更新镜像

当 ACR 仓库中有新镜像时：

```bash
# 拉取最新镜像
docker-compose pull

# 重新启动服务
docker-compose up -d
```

#### 6. Docker Compose 配置说明

完整的 `docker-compose.yml` 配置如下：

```yaml
services:
  # TKLine 应用服务
  app:
    image: registry.cn-hangzhou.aliyuncs.com/bytelab/tkline-server:latest
    container_name: tkline-server
    restart: unless-stopped
    environment:
      # 服务器配置
      SERVER_PORT: ${SERVER_PORT:-8080}

      # 数据库配置（连接到外部 MySQL）
      DB_HOST: ${DB_HOST:-localhost}
      DB_PORT: ${DB_PORT:-3306}
      DB_NAME: ${DB_NAME:-tkline}
      DB_USERNAME: ${DB_USERNAME:-root}
      DB_PASSWORD: ${DB_PASSWORD}

      # JWT配置
      # 生产环境必须修改此密钥！建议使用 64 位随机字符串
      # 生成方法: openssl rand -hex 64
      JWT_SECRET: ${JWT_SECRET:-25ce66ee090fc4e2d272289dc1a2e54d692cfbd35bbf96e60b2c308b3a0b2a15f7d7f38051ccff27038d5fc397dd09badc2c4ce52d089867c957cfbaa94c475e}

      # 邮箱配置
      MAIL_HOST: ${MAIL_HOST:-smtpdm.aliyun.com}
      MAIL_PORT: ${MAIL_PORT:-465}
      MAIL_USERNAME: ${MAIL_USERNAME:-noreply@mail.example.com}
      MAIL_PASSWORD: ${MAIL_PASSWORD:-your_mail_password}
      MAIL_PROTOCOL: ${MAIL_PROTOCOL:-smtp}
      MAIL_REPLYTO: ${MAIL_REPLYTO:-example@126.com}

      # SpringDoc 配置
      # 生产环境建议设置为 false
      SPRINGDOC_ENABLED: ${SPRINGDOC_ENABLED:-true}

      # 日志配置
      # 可选值: trace, debug, info, warn, error
      LOG_LEVEL_APP: ${LOG_LEVEL_APP:-info}
      LOG_LEVEL_MYBATIS: ${LOG_LEVEL_MYBATIS:-warn}

      # 时区配置
      TZ: Asia/Shanghai
    ports:
      - "${SERVER_PORT:-8080}:8080"
    network_mode: host  # 使用宿主机网络，便于连接外部数据库
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

**配置说明**：

- **镜像地址**：`registry.cn-hangzhou.aliyuncs.com/bytelab/tkline-server:latest` - 阿里云 ACR 镜像
- **容器名称**：`tkline-server` - 便于识别和管理
- **重启策略**：`unless-stopped` - 容器异常退出时自动重启
- **网络模式**：`host` - 使用宿主机网络，便于访问外部数据库
- **健康检查**：通过 `/actuator/health` 端点检查应用健康状态
- **环境变量**：支持通过 `.env` 文件或环境变量配置，格式为 `${变量名:-默认值}`

### 方式二：本地开发环境启动

#### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

#### 2. 数据库配置

创建数据库：

```sql
CREATE DATABASE tkline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 3. 配置文件

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tkline?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

#### 4. 运行项目

```bash
mvn spring-boot:run
```

项目启动后访问：

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## API 端点

### 认证相关

- `POST /api/user/login` - 用户登录
- `POST /api/user/create` - 用户注册
- `POST /api/user/refresh-token` - 刷新 Token
- `POST /api/user/change-password` - 修改密码

### 用户管理

- `GET /api/user/{id}` - 获取用户信息
- `GET /api/user/username/{username}` - 按用户名查询
- `GET /api/user/list` - 用户列表
- `POST /api/user/update` - 更新用户信息
- `POST /api/user/delete` - 删除用户

### 安全密钥

- `GET /api/security/keys/public-key` - 获取 RSA 公钥

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SERVER_PORT` | 服务端口 | 8081 |
| `DB_HOST` | 数据库地址 | localhost |
| `DB_PORT` | 数据库端口 | 3306 |
| `DB_NAME` | 数据库名称 | tkline |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | root |
| `JWT_SECRET` | JWT 签名密钥 | (默认值) |

## 安全特性

- **RSA 加密传输**：敏感信息（如密码）使用 RSA 加密传输
- **BCrypt 密码散列**：密码使用 BCrypt 算法散列存储
- **JWT Token 认证**：基于 JWT 的无状态认证
- **Token 自动续期**：Token 即将过期时自动续期

## 项目结构

```
src/main/java/com/bytelab/tkline/server/
├── annotation/          # 自定义注解
├── common/              # 通用响应类
├── config/              # 配置类
├── constant/            # 常量定义
├── controller/          # 控制层
├── converter/           # MapStruct转换器
├── dto/                 # 数据传输对象
├── entity/              # 数据库实体
├── enums/               # 枚举定义
├── exception/           # 异常类
├── filter/              # 过滤器
├── handler/             # 处理器
├── interceptor/         # 拦截器
├── mapper/              # MyBatis Mapper
├── service/             # 服务层
└── util/                # 工具类
```

## License

MIT
