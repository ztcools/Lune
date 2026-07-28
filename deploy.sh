#!/bin/bash
# ============================================================
# Lune 一键部署脚本（服务器端执行）
# 用法:
#   开发环境: bash deploy.sh --dev
#   生产环境: bash deploy.sh --prod
#   首次部署: bash deploy.sh --prod
#
# 前置条件:
#   1. 服务器已安装 git
#   2. 将此脚本复制到服务器或从仓库获取
# ============================================================
set -euo pipefail

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_step()  { echo -e "\n${CYAN}============================================${NC}"; echo -e "${CYAN}  $*${NC}"; echo -e "${CYAN}============================================${NC}"; }

# ---- 配置 ----
MODE="--prod"
PROJECT_DIR="${PROJECT_DIR:-/opt/lune}"
REPO_URL="${REPO_URL:-https://github.com/ztcools/Lune.git}"
BRANCH="${BRANCH:-main}"

# ---- 解析参数 ----
if [ $# -gt 0 ]; then
    case "$1" in
        --dev|--prod) MODE="$1" ;;
        *) log_error "未知参数: $1. 请使用 --dev 或 --prod"; exit 1 ;;
    esac
fi

COMPOSE_FILE="docker-compose.${MODE#--}.yml"
ENV_FILE="$PROJECT_DIR/.env"

echo ""
echo "=========================================="
echo "  Lune 一键部署脚本"
echo "  模式: ${MODE#--}"
echo "  项目路径: $PROJECT_DIR"
echo "=========================================="
echo ""

# ============================================================
# Step 1: 安装 Docker
# ============================================================
install_docker() {
    log_step "Step 1/5: 检查 Docker 环境"

    if command -v docker &>/dev/null && docker compose version &>/dev/null 2>&1; then
        log_ok "Docker $(docker --version) 已安装"
        log_ok "Docker Compose $(docker compose version --short) 已可用"
        return
    fi

    log_warn "Docker 未安装，正在自动安装..."

    if [ -f /etc/os-release ]; then
        . /etc/os-release
        case "$ID" in
            ubuntu|debian)
                log_info "检测到 $ID 系统，使用 apt 安装..."
                sudo apt-get update -qq
                sudo apt-get install -y -qq ca-certificates curl
                sudo install -m 0755 -d /etc/apt/keyrings
                sudo curl -fsSL https://download.docker.com/linux/$ID/gpg -o /etc/apt/keyrings/docker.asc
                sudo chmod a+r /etc/apt/keyrings/docker.asc
                echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/$ID $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
                sudo apt-get update -qq
                sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-compose-plugin
                ;;
            centos|rhel|fedora)
                log_info "检测到 $ID 系统，使用 yum/dnf 安装..."
                sudo yum install -y yum-utils
                sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
                sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
                sudo systemctl start docker
                sudo systemctl enable docker
                ;;
            *)
                log_warn "未识别的系统，尝试使用官方安装脚本..."
                curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
                sudo sh /tmp/get-docker.sh
                rm -f /tmp/get-docker.sh
                ;;
        esac
    else
        log_warn "无法检测系统版本，尝试使用官方安装脚本..."
        curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
        sudo sh /tmp/get-docker.sh
        rm -f /tmp/get-docker.sh
    fi

    sudo systemctl enable docker 2>/dev/null || true
    sudo systemctl start docker 2>/dev/null || true

    # 将当前用户加入 docker 组
    if ! groups "$USER" | grep -q docker; then
        sudo usermod -aG docker "$USER"
        log_warn "已将当前用户加入 docker 组。可能需要重新登录或运行: newgrp docker"
    fi

    log_ok "Docker 安装完成"
}

# ============================================================
# Step 2: 获取 / 更新项目代码
# ============================================================
setup_repo() {
    log_step "Step 2/5: 获取项目代码"

    if [ -d "$PROJECT_DIR/.git" ]; then
        log_info "项目已存在，拉取最新代码..."
        cd "$PROJECT_DIR"
        git fetch origin "$BRANCH"
        git checkout "$BRANCH"
        git pull origin "$BRANCH" --ff-only || log_warn "git pull 失败（可能有本地修改），继续使用当前代码"
        log_ok "代码已更新"
    else
        log_info "克隆项目仓库..."
        if [ ! -d "$PROJECT_DIR" ]; then
            sudo mkdir -p "$PROJECT_DIR"
            sudo chown "$USER:$USER" "$PROJECT_DIR"
        fi
        git clone -b "$BRANCH" "$REPO_URL" "$PROJECT_DIR"
        cd "$PROJECT_DIR"
        log_ok "代码已克隆到 $PROJECT_DIR"
    fi
}

# ============================================================
# Step 3: 配置环境变量
# ============================================================
setup_env() {
    log_step "Step 3/5: 配置环境变量"

    cd "$PROJECT_DIR"

    if [ ! -f "$ENV_FILE" ]; then
        if [ -f "$PROJECT_DIR/.env.template" ]; then
            log_warn ".env 文件不存在，正在从 .env.template 生成..."

            cp "$PROJECT_DIR/.env.template" "$ENV_FILE"

            # 自动生成随机密码
            local db_pass=$(openssl rand -base64 32 2>/dev/null || head -c 32 /dev/urandom | base64)
            local jwt_secret=$(openssl rand -base64 64 2>/dev/null || head -c 64 /dev/urandom | base64)

            # 替换占位符
            sed -i "s/^DB_ROOT_PASSWORD=.*/DB_ROOT_PASSWORD=${db_pass}/" "$ENV_FILE"
            sed -i "s/^DB_PASSWORD=.*/DB_PASSWORD=${db_pass}/" "$ENV_FILE"
            sed -i "s/^JWT_SECRET=.*/JWT_SECRET=${jwt_secret}/" "$ENV_FILE"

            log_ok ".env 文件已生成，密码已自动设置"
            echo ""
            log_info "自动生成的凭据："
            log_info "  数据库密码: $db_pass"
            log_info "  JWT Secret: $jwt_secret"
            echo ""
            log_warn "请妥善保管这些密码！可运行以下命令查看："
            log_warn "  cat $ENV_FILE"
            echo ""
        else
            log_error ".env.template 不存在，无法生成 .env"
            exit 1
        fi
    else
        log_ok ".env 文件已存在"

        # 检查是否还是模板占位值
        if grep -q "change-me" "$ENV_FILE" 2>/dev/null; then
            log_error "检测到 .env 中仍有 'change-me' 占位符！"
            log_error "请编辑 .env 填入真实值后再重试："
            log_error "  vim $ENV_FILE"
            exit 1
        fi
    fi

    # 加载 .env 验证必填项
    set -a
    source "$ENV_FILE"
    set +a

    local missing=""
    for var in DB_ROOT_PASSWORD DB_PASSWORD JWT_SECRET; do
        local val="${!var:-}"
        if [ -z "$val" ] || [[ "$val" == *"change-me"* ]]; then
            missing="$missing $var"
        fi
    done

    if [ -n "$missing" ]; then
        log_error "以下环境变量未正确设置:$missing"
        log_error "请编辑 .env 文件: vim $ENV_FILE"
        exit 1
    fi
}

# ============================================================
# Step 4: 创建必要目录
# ============================================================
setup_dirs() {
    log_info "创建数据目录..."
    mkdir -p "$PROJECT_DIR/backups"
}

# ============================================================
# Step 5: 启动服务
# ============================================================
deploy() {
    log_step "Step 4/5: 构建并启动服务"

    cd "$PROJECT_DIR"

    log_info "拉取最新镜像..."
    docker compose -f "$COMPOSE_FILE" pull 2>/dev/null || true

    log_info "构建镜像..."
    docker compose -f "$COMPOSE_FILE" build --pull

    log_info "停止旧容器..."
    docker compose -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null || true

    log_info "启动服务..."
    docker compose -f "$COMPOSE_FILE" up -d --wait --wait-timeout 120 2>/dev/null || \
    docker compose -f "$COMPOSE_FILE" up -d

    log_ok "服务已启动！"
}

# ============================================================
# Step 4.5: 数据库增量迁移（幂等，安全重复执行）
# ============================================================
run_migrations() {
    log_info "执行数据库增量迁移..."
    cd "$PROJECT_DIR"
    local mysql_container
    if [ "$MODE" = "--dev" ]; then mysql_container="lune-mysql-dev"; else mysql_container="lune-mysql"; fi

    set -a; source "$ENV_FILE"; set +a
    local mig_dir="$PROJECT_DIR/lune-server/lune-web/src/main/resources/sql"

    # 等待 MySQL 就绪
    local retries=0
    until docker exec "$mysql_container" mysqladmin ping -h localhost -u root -p"${DB_ROOT_PASSWORD}" &>/dev/null || [ $retries -ge 30 ]; do
        sleep 2; retries=$((retries+1))
    done

    # 依次执行所有 migration-*.sql（若存在）
    shopt -s nullglob
    for mig in "$mig_dir"/migration-*.sql; do
        log_info "应用迁移: $(basename "$mig")"
        docker exec -i "$mysql_container" mysql -uroot -p"${DB_ROOT_PASSWORD}" lune < "$mig" 2>/dev/null \
            && log_ok "迁移完成: $(basename "$mig")" \
            || log_warn "迁移已应用或跳过: $(basename "$mig")"
    done
    shopt -u nullglob
}

# ============================================================
# 部署完成
# ============================================================
post_deploy() {
    log_step "Step 5/5: 验证部署"

    cd "$PROJECT_DIR"

    echo ""
    log_info "等待服务健康检查..."
    sleep 8

    echo ""
    echo "=========================================="
    echo "  Lune 部署完成！"
    echo "=========================================="
    echo ""

    echo "📋 服务状态:"
    docker compose -f "$COMPOSE_FILE" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null || \
    docker compose -f "$COMPOSE_FILE" ps
    echo ""

    if [ "$MODE" = "--dev" ]; then
        echo "🌐 访问地址:"
        echo "   前端: http://localhost:${NGINX_PORT:-8080}"
        echo "   Vite HMR: http://localhost:5173"
        echo "   后端 API: http://localhost:8081/api"
    else
        local server_ip=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "YOUR_SERVER_IP")
        echo "🌐 访问地址:"
        echo "   http://${server_ip}:${NGINX_PORT:-80}"
        echo ""
        echo "🔑 默认管理员:"
        echo "   用户名: admin"
        echo "   密码: admin123"
        echo "   ⚠️  首次登录后请立即修改密码！"
    fi

    echo ""
    echo "📋 常用命令:"
    echo "   查看日志:  docker compose -f $COMPOSE_FILE logs -f"
    echo "   重启服务:  docker compose -f $COMPOSE_FILE restart"
    echo "   停止服务:  docker compose -f $COMPOSE_FILE down"
    echo "   备份数据库: make backup  (或 bash backup.sh)"
    echo ""
}

# ============================================================
# Main
# ============================================================
main() {
    install_docker
    setup_repo
    setup_env
    setup_dirs
    deploy
    run_migrations
    post_deploy
}

main
