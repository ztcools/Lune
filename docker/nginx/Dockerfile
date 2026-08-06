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
# lune-common.conf 是 server 级公共配置，被 lune.conf 内 include（HTTP / 预留的
# HTTPS server 共用一份），缺了它 nginx 直接起不来 —— 两个文件必须一起 COPY
COPY docker/nginx/nginx.prod.conf /etc/nginx/conf.d/lune.conf
COPY docker/nginx/lune-common.conf /etc/nginx/lune-common.conf

# 从构建阶段复制前端产物
COPY --from=frontend-build /build/dist/ /usr/share/nginx/html/

# 健康检查（用域名 Host 头避免被 IP 拒绝策略拦截）
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f -H "Host: ztcools.com" http://127.0.0.1/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
