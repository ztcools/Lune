#!/bin/bash
# ============================================================
# Lune 数据库备份脚本
# 用法:
#   bash backup.sh                  # 手动备份
#   bash backup.sh --cron           # 设置 crontab 每天凌晨 3 点自动备份
#
# 备份文件: ./backups/lune_YYYYMMDD_HHMMSS.sql.gz
# 默认保留最近 7 天的备份
# ============================================================
set -euo pipefail

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }
log_error(){ echo -e "${RED}[ERROR]${NC} $*"; }

# ---- 配置 ----
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKUP_DIR="$PROJECT_DIR/backups"
RETENTION_DAYS=7
CONTAINER="${MYSQL_CONTAINER:-lune-mysql}"

# ---- 从 .env 读取配置 ----
if [ -f "$PROJECT_DIR/.env" ]; then
    set -a
    source "$PROJECT_DIR/.env"
    set +a
else
    log_error ".env 文件不存在，无法获取数据库密码"
    exit 1
fi

DB_PASS="${DB_ROOT_PASSWORD:-${DB_PASSWORD:-}}"
if [ -z "$DB_PASS" ]; then
    log_error "未找到数据库密码（DB_ROOT_PASSWORD 或 DB_PASSWORD）"
    exit 1
fi

# ============================================================
# 备份函数
# ============================================================
do_backup() {
    mkdir -p "$BACKUP_DIR"

    local timestamp=$(date +%Y%m%d_%H%M%S)
    local filename="lune_${timestamp}.sql.gz"
    local filepath="$BACKUP_DIR/$filename"

    log_info "开始备份数据库 lune..."

    if docker exec "$CONTAINER" mysqldump \
        -u root \
        -p"$DB_PASS" \
        --single-transaction \
        --quick \
        --lock-tables=false \
        lune 2>/dev/null | gzip > "$filepath"; then

        local size=$(du -h "$filepath" | cut -f1)
        log_ok "备份完成: $filename ($size)"

        # 清理旧备份
        log_info "清理 ${RETENTION_DAYS} 天前的旧备份..."
        find "$BACKUP_DIR" -name "lune_*.sql.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true

        # 统计
        local count=$(find "$BACKUP_DIR" -name "lune_*.sql.gz" | wc -l)
        local total_size=$(du -sh "$BACKUP_DIR" 2>/dev/null | cut -f1)
        log_ok "当前保留 $count 个备份，总大小 $total_size"
    else
        log_error "备份失败！请检查容器是否运行"
        exit 1
    fi
}

# ============================================================
# 设置 crontab
# ============================================================
setup_cron() {
    log_info "设置 crontab 自动备份（每天凌晨 3:00）..."

    local script_path="$(readlink -f "$0")"
    local cron_line="0 3 * * * /bin/bash $script_path >> $BACKUP_DIR/backup.log 2>&1"

    # 检查是否已存在
    if crontab -l 2>/dev/null | grep -qF "$script_path"; then
        log_info "crontab 已配置，跳过"
        crontab -l 2>/dev/null | grep -F "$script_path"
        return
    fi

    (crontab -l 2>/dev/null || true; echo "$cron_line") | crontab -
    log_ok "crontab 已设置"
    log_info "当前 crontab:"
    crontab -l 2>/dev/null || true
}

# ============================================================
# Main
# ============================================================

case "${1:-}" in
    --cron)
        setup_cron
        ;;
    *)
        do_backup
        ;;
esac
