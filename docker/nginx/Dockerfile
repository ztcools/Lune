# ============================================================
# Lune Nginx - 生产环境 Dockerfile
# 多阶段构建：Vue 前端构建 + Nginx 服务
# ============================================================

# ========== STAGE 1: VUE FRONTEND BUILD ==========
FROM node:20-alpine AS frontend-build

WORKDIR /build

# 复制 package 文件并安装依赖
COPY lune-ui/package*.json /build/
RUN npm ci

# 复制前端源码
COPY lune-ui/ /build/

# 构建生产版本
RUN npm run build

# ========== STAGE 2: NGINX RUNTIME ==========
FROM nginx:1.27-alpine

# 安装 curl 用于健康检查
RUN apk add --no-cache curl

# 删除默认配置
RUN rm -f /etc/nginx/conf.d/default.conf

# 复制自定义 nginx 配置
COPY docker/nginx/nginx.prod.conf /etc/nginx/conf.d/lune.conf

# 从构建阶段复制前端产物
COPY --from=frontend-build /build/dist/ /usr/share/nginx/html/

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:80/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
