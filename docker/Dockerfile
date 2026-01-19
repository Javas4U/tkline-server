# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
# 启用并行下载和离线模式准备
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -T 1C

# 复制源代码并构建
COPY src ./src
# 启用多线程构建（1C = 每个 CPU 核心 1 个线程）
# 使用缓存挂载加速 Maven 构建
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B -T 1C

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非 root 用户
RUN addgroup -g 1000 tkline && \
    adduser -D -u 1000 -G tkline tkline

# 从构建阶段复制 jar 文件
COPY --from=build /app/target/*.jar app.jar

# 修改文件所有者
RUN chown -R tkline:tkline /app

# 切换到非 root 用户
USER tkline

# 暴露端口（根据应用配置修改）
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
