# ============================================================
# Lune Backend - 多阶段构建 Dockerfile
# 构建: docker build -f docker/backend/Dockerfile -t lune-backend .
# ============================================================

# ========== STAGE 1: MAVEN BUILD ==========
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

# 先复制 POM 文件（利用 Docker 缓存层，POM 不常变）
COPY lune-server/pom.xml /build/pom.xml
COPY lune-server/lune-web/pom.xml /build/lune-web/pom.xml

# 下载依赖（此层仅在 POM 变化时重建）
RUN mvn dependency:go-offline -B -q 2>/dev/null || true

# 复制源码
COPY lune-server/ /build/

# 构建（跳过测试）
RUN mvn clean package -DskipTests -B -q

# ========== STAGE 2: JRE RUNTIME ==========
FROM eclipse-temurin:17-jre-alpine AS runtime

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 创建非 root 用户
RUN addgroup -g 1000 lune && \
    adduser -u 1000 -G lune -s /bin/sh -D lune

# 创建应用目录
RUN mkdir -p /app/upload && \
    chown -R lune:lune /app

WORKDIR /app

# 从构建阶段复制 JAR
COPY --from=build /build/lune-web/target/*.jar /app/lune.jar

# 切换到非 root 用户
USER lune:lune

# JVM 参数（适配 2核4G 服务器）
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=50.0 \
  -XX:MaxMetaspaceSize=128m \
  -XX:+UseG1GC \
  -XX:+ExitOnOutOfMemoryError"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD curl -f http://localhost:8081/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/lune.jar"]
