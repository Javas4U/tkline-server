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

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

创建数据库：

```sql
CREATE DATABASE tkline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置文件

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tkline?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 4. 运行项目

```bash
mvn spring-boot:run
```

项目启动后访问：

- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

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
