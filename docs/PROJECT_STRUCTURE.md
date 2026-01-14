# TKLine Server - 项目结构说明

## 项目概述

TKLine Server 是从 apex-tunnel-server 项目复制并简化的后端服务，专注于用户认证和管理功能。

## 目录结构

```
tkline-server/
├── src/main/java/com/bytelab/tkline/server/
│   ├── TklineServerApplication.java    # 主启动类
│   │
│   ├── annotation/                      # 自定义注解
│   │   ├── BCrypt.java                  # BCrypt密码散列注解
│   │   ├── Decrypt.java                 # RSA解密注解
│   │   └── Encrypt.java                 # 字段加密注解
│   │
│   ├── advice/                          # 请求/响应拦截
│   │   └── DecryptRequestBodyAdvice.java # 请求体自动解密
│   │
│   ├── common/                          # 通用类
│   │   └── ApiResult.java               # 统一响应格式
│   │
│   ├── config/                          # 配置类
│   │   ├── SecurityConfig.java          # Spring Security配置
│   │   ├── MapStructConfig.java         # MapStruct配置
│   │   ├── WebMvcConfig.java            # Web MVC配置
│   │   └── JacksonConfig.java           # JSON序列化配置
│   │
│   ├── constant/                        # 常量定义
│   │   ├── SecurityConstants.java       # 安全相关常量
│   │   └── CacheConstants.java          # 缓存相关常量
│   │
│   ├── controller/                      # 控制器层
│   │   └── core/
│   │       ├── UserController.java      # 用户管理接口
│   │       └── SecurityKeyController.java # 安全密钥接口
│   │
│   ├── converter/                       # DTO转换器(MapStruct)
│   │   ├── UserConverter.java           # 用户转换器
│   │   └── ...                          # 其他转换器
│   │
│   ├── dto/                             # 数据传输对象
│   │   ├── auth/                        # 认证相关DTO
│   │   │   └── TokenInfo.java           # Token信息
│   │   ├── security/                    # 安全相关DTO
│   │   │   ├── PublicKeyDTO.java        # RSA公钥DTO
│   │   │   └── ...
│   │   ├── user/                        # 用户相关DTO
│   │   │   ├── UserInfoDTO.java         # 用户信息
│   │   │   ├── LoginRequest.java        # 登录请求
│   │   │   ├── LoginResponse.java       # 登录响应
│   │   │   ├── CreateUserRequest.java   # 创建用户请求
│   │   │   ├── UpdateUserRequest.java   # 更新用户请求
│   │   │   └── ...
│   │   ├── BaseDTO.java                 # 基础DTO(审计字段)
│   │   └── BaseResponse.java            # 基础响应类
│   │
│   ├── entity/                          # 数据库实体
│   │   ├── SysUser.java                 # 用户实体
│   │   ├── RsaKeyPair.java              # RSA密钥对
│   │   └── ...                          # 其他实体
│   │
│   ├── enums/                           # 枚举类
│   │   ├── UserRole.java                # 用户角色枚举
│   │   ├── AccountStatus.java           # 账户状态枚举
│   │   └── ...
│   │
│   ├── exception/                       # 异常类
│   │   ├── BusinessException.java       # 业务异常
│   │   └── UnauthorizedException.java   # 未授权异常
│   │
│   ├── filter/                          # 过滤器
│   │   └── JwtAuthenticationFilter.java # JWT认证过滤器
│   │
│   ├── handler/                         # 处理器
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   ├── BCryptTypeHandler.java       # BCrypt类型处理器
│   │   ├── EncryptTypeHandler.java      # 加密类型处理器
│   │   └── AuditMetaObjectHandler.java  # 审计字段自动填充
│   │
│   ├── interceptor/                     # 拦截器
│   │   └── TraceIdInterceptor.java      # 请求追踪ID拦截器
│   │
│   ├── mapper/                          # MyBatis Mapper
│   │   └── core/
│   │       ├── UserMapper.java          # 用户Mapper
│   │       └── RsaKeyPairMapper.java    # RSA密钥对Mapper
│   │
│   ├── service/                         # 服务层
│   │   ├── core/                        # 核心服务
│   │   │   ├── UserService.java         # 用户服务接口
│   │   │   ├── impl/
│   │   │   │   ├── UserServiceImpl.java # 用户服务实现
│   │   │   │   └── ...
│   │   │   ├── SecurityKeyService.java  # 安全密钥服务
│   │   │   └── TokenCacheService.java   # Token缓存服务
│   │   ├── modules/                     # 模块服务
│   │   │   └── EmailService.java        # 邮件服务
│   │   └── user/                        # 用户相关服务
│   │       ├── UserCacheService.java    # 用户缓存服务
│   │       ├── LoginLogService.java     # 登录日志服务
│   │       └── ...
│   │
│   └── util/                            # 工具类
│       ├── JwtUtil.java                 # JWT工具类
│       ├── SecurityUtils.java           # 安全工具类
│       └── RsaUtil.java                 # RSA加密工具类
│
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   └── mapper/                          # MyBatis XML映射文件
│
├── pom.xml                              # Maven配置
├── README.md                            # 项目说明
└── .gitignore                           # Git忽略配置

```

## 核心功能模块

### 1. 用户认证 (Authentication)

**涉及文件:**
- `controller/core/UserController.java`
- `service/core/impl/UserServiceImpl.java`
- `filter/JwtAuthenticationFilter.java`
- `util/JwtUtil.java`

**功能:**
- 用户登录 (`POST /api/user/login`)
- 用户注册 (`POST /api/user/create`)
- Token刷新 (`POST /api/user/refresh-token`)
- 密码修改 (`POST /api/user/change-password`)

### 2. 用户管理 (User Management)

**涉及文件:**
- `controller/core/UserController.java`
- `service/core/impl/UserServiceImpl.java`
- `entity/SysUser.java`

**功能:**
- 获取用户信息 (`GET /api/user/{id}`)
- 用户列表查询 (`GET /api/user/list`)
- 更新用户信息 (`POST /api/user/update`)
- 删除用户 (`POST /api/user/delete`)

### 3. 安全加密 (Security & Encryption)

**涉及文件:**
- `controller/core/SecurityKeyController.java`
- `service/core/SecurityKeyService.java`
- `util/RsaUtil.java`
- `annotation/Decrypt.java`
- `advice/DecryptRequestBodyAdvice.java`

**功能:**
- RSA公钥获取 (`GET /api/security/keys/public-key`)
- 请求体自动解密
- 密码BCrypt散列

### 4. 数据转换 (Data Conversion)

**涉及文件:**
- `converter/UserConverter.java`
- `config/MapStructConfig.java`

**功能:**
- Entity ↔ DTO自动转换
- 使用MapStruct框架

### 5. 异常处理 (Exception Handling)

**涉及文件:**
- `handler/GlobalExceptionHandler.java`
- `exception/BusinessException.java`

**功能:**
- 统一异常处理
- 标准化错误响应格式

## 关键技术特性

### 1. Spring Security 集成
- JWT Token认证
- 无状态会话管理
- 白名单配置

### 2. MyBatis-Plus
- 自动代码生成
- 逻辑删除支持
- 分页查询

### 3. MapStruct
- 编译期类型安全转换
- 零运行时开销

### 4. RSA加密传输
- 前端使用公钥加密敏感信息
- 后端自动解密(@Decrypt注解)

### 5. BCrypt密码散列
- 密码安全存储
- 自动散列(@BCrypt注解)

## 数据库表结构

主要表：
- `sys_user` - 用户表
- `rsa_key_pair` - RSA密钥对表
- (其他业务表根据需要扩展)

## API文档

项目集成了SpringDoc OpenAPI，启动后访问：
- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

## 配置说明

### application.yml 主要配置项

```yaml
server:
  port: 8081                           # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tkline  # 数据库连接
    username: root
    password: root

jwt:
  secret: xxx                          # JWT签名密钥
  expiration: 1800                     # Token过期时间(秒)

security:
  max-devices: 5                       # 单用户最大登录设备数
  max-login-attempts: 5                # 最大登录失败次数
  lock-duration-minutes: 15            # 账号锁定时长

cache:
  user:
    max-size: 10000                    # 用户缓存最大数量
    expire-hours: 24                   # 缓存过期时间
```

## 依赖管理

主要依赖(pom.xml)：
- Spring Boot 3.5.6
- Spring Security
- MyBatis-Plus 3.5.5
- jjwt 0.12.3 (JWT)
- MapStruct 1.6.3
- MySQL Connector 8.2.0
- SpringDoc OpenAPI 2.7.0

## 下一步扩展

如需添加更多业务功能，建议遵循现有架构：
1. 在 `entity/` 中创建数据库实体
2. 在 `mapper/` 中创建MyBatis接口
3. 在 `dto/` 中创建请求/响应DTO
4. 在 `converter/` 中创建MapStruct转换器
5. 在 `service/` 中实现业务逻辑
6. 在 `controller/` 中暴露REST API

## 项目来源

本项目基于 apex-tunnel-server 项目骨架创建，保留了用户认证和管理相关的核心功能。
