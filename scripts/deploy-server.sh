#!/bin/bash
# ============================================================
# Lune 服务器端部署脚本（由 CI/CD 经 SSH 调用）
#
# 前置：CI 已把以下文件 scp 到 /opt/lune/deploy-tmp/：
#   lune-backend.tar.gz / lune-nginx.tar.gz / lune-agent.tar.gz
#   docker-compose.server.yml
#   migration-*.sql
#   deploy-server.sh（本文件）
#
# 职责：加载镜像 → 替换 compose → 校验 → 迁移 → 启动 → 健康检查 → 清理
# 全程幂等，可重复执行。镜像标签固定 :latest，靠 image ID 变化触发容器重建。
# ============================================================
set -euo pipefail

LUNE_HOME="${LUNE_HOME:-/opt/lune}"
TMP_DIR="$LUNE_HOME/deploy-tmp"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; CYAN='\033[0;36m'; NC='\033[0m'
log()   { echo -e "${CYAN}[deploy]${NC} $*"; }
ok()    { echo -e "${GREEN}[deploy]${NC} $*"; }
warn()  { echo -e "${YELLOW}[deploy]${NC} $*"; }

cd "$LUNE_HOME"

# ------------------------------------------------------------
# 1. 加载镜像
# ------------------------------------------------------------
log "加载镜像 ..."
for img in lune-backend lune-nginx lune-agent; do
    if [ -f "$TMP_DIR/$img.tar.gz" ]; then
        gunzip -c "$TMP_DIR/$img.tar.gz" | docker load
        ok "已加载 $img:latest"
    else
        warn "跳过 $img（未找到 $TMP_DIR/$img.tar.gz）"
    fi
done

# ------------------------------------------------------------
# 2. 备份并替换 docker-compose.yml
# ------------------------------------------------------------
if [ -f "$TMP_DIR/docker-compose.server.yml" ]; then
    log "替换 docker-compose.yml ..."
    cp docker-compose.yml "docker-compose.yml.bak-$(date +%Y%m%d-%H%M%S)" 2>/dev/null || true
    cp "$TMP_DIR/docker-compose.server.yml" docker-compose.yml
else
    warn "未找到 docker-compose.server.yml，沿用现有 compose"
fi

# ------------------------------------------------------------
# 3. 校验 compose（先校验再起，避免坏配置打挂线上）
# ------------------------------------------------------------
log "校验 compose 配置 ..."
docker compose --env-file .env config -q

# ------------------------------------------------------------
# 4. 数据库增量迁移（全部幂等，重复执行是空操作）
# ------------------------------------------------------------
log "执行数据库增量迁移 ..."
if compgen -G "$TMP_DIR/migration-*.sql" >/dev/null 2>&1; then
    set -a; . ./.env; set +a
    for f in "$TMP_DIR"/migration-*.sql; do
        printf '%s\n' "  应用 $(basename "$f") ..."
        if docker exec -i lune-mysql mysql --default-character-set=utf8mb4 \
                -uroot -p"$DB_ROOT_PASSWORD" lune < "$f" 2>/dev/null; then
            ok "  迁移完成: $(basename "$f")"
        else
            warn "  已应用或跳过: $(basename "$f")"
        fi
    done
else
    log "  无待执行迁移脚本"
fi

# ------------------------------------------------------------
# 5. 启动服务
# ------------------------------------------------------------
log "启动服务 ..."
docker compose --env-file .env up -d

# ------------------------------------------------------------
# 6. 后端健康检查
# ------------------------------------------------------------
log "等待后端健康检查 ..."
healthy=0
for i in $(seq 1 30); do
    if docker exec lune-backend curl -fsS http://localhost:8081/api/actuator/health >/dev/null 2>&1; then
        ok "后端健康检查通过"
        healthy=1
        break
    fi
    sleep 2
done
if [ "$healthy" != "1" ]; then
    echo -e "${RED}[deploy] 后端健康检查超时！${NC}" >&2
    docker compose --env-file .env ps
    exit 1
fi

# ------------------------------------------------------------
# 7. 清理临时镜像包 + 悬空镜像
# ------------------------------------------------------------
log "清理临时文件与悬空镜像 ..."
rm -f "$TMP_DIR"/*.tar.gz
docker image prune -f >/dev/null 2>&1 || true

echo ""
ok "部署完成，当前服务状态："
docker compose --env-file .env ps
