#!/usr/bin/env bash
# =====================================================================
# 清理素材库死指针 + 修复失效的站点 logo
#
# 为什么要单独一个脚本、而不是写进 migration-*.sql：
# 「这条 resource 记录是不是死指针」取决于那台机器的 upload 卷里有没有那个文件。
# 生产库里 16 条 resource 有 15 条指向不存在的文件（后台「选择图片」弹窗里就是一排
# 裂图，站点 logo 也 404 导致标签页图标空白）；而本地 dev 库里同一批文件是真实存在
# 的。同一段无条件 DELETE 在前者是修复、在后者是破坏 —— deploy.sh 会自动跑
# migration-*.sql，把这种依赖环境的操作放进去等于埋雷。
#
# 所以这里先进容器 stat 文件，只处理确认不存在的那些。
#
# 用法：
#   bash prune-orphan-resources.sh              # 只报告，不改（默认）
#   bash prune-orphan-resources.sh --apply      # 真正执行
#   MYSQL_CONTAINER=lune-mysql-dev BACKEND_CONTAINER=lune-backend-dev \
#     bash prune-orphan-resources.sh --apply    # 指定本地 dev 容器名
# =====================================================================
set -euo pipefail

APPLY=false
[[ "${1:-}" == "--apply" ]] && APPLY=true

MYSQL_CONTAINER="${MYSQL_CONTAINER:-lune-mysql}"
BACKEND_CONTAINER="${BACKEND_CONTAINER:-lune-backend}"
UPLOAD_DIR="${UPLOAD_DIR:-/app/upload}"
ENV_FILE="${ENV_FILE:-.env}"

[[ -f "$ENV_FILE" ]] || { echo "找不到 $ENV_FILE（请在项目/部署目录下运行）" >&2; exit 1; }
# shellcheck disable=SC1090
set -a; . "$ENV_FILE"; set +a
: "${DB_ROOT_PASSWORD:?$ENV_FILE 里没有 DB_ROOT_PASSWORD}"

mysql_q() {
    docker exec -i "$MYSQL_CONTAINER" \
        mysql --default-character-set=utf8mb4 -uroot -p"$DB_ROOT_PASSWORD" -N -B lune "$@" 2>/dev/null
}

# ---- 1. 找出 resource 表里指向不存在文件的记录 -----------------------
mapfile -t paths < <(mysql_q -e "SELECT path FROM resource WHERE store_type = 'local'")
if [[ ${#paths[@]} -eq 0 ]]; then
    echo "resource 表里没有本地存储的记录，无需处理"
    exit 0
fi

# 一次性把卷里的文件列出来，避免每条记录都 docker exec（几十次很慢）
existing=$(docker exec "$BACKEND_CONTAINER" sh -c "ls -1 '$UPLOAD_DIR' 2>/dev/null || true")

orphans=()
for p in "${paths[@]}"; do
    f="${p##*/}"
    grep -qxF "$f" <<<"$existing" || orphans+=("$p")
done

echo "resource 本地记录：${#paths[@]} 条，其中死指针 ${#orphans[@]} 条"
for o in "${orphans[@]}"; do echo "  ✗ $o"; done

# ---- 2. 检查站点 logo（同时被当作 favicon，见 stores/app.js）---------
logo=$(mysql_q -e "SELECT config_value FROM site_config WHERE config_key = 'site_logo'")
logo_broken=false
if [[ -n "$logo" && "$logo" == /upload/* ]]; then
    if ! grep -qxF "${logo##*/}" <<<"$existing"; then
        logo_broken=true
        echo "站点 logo 指向的文件不存在：$logo"
    fi
fi

if [[ ${#orphans[@]} -eq 0 && "$logo_broken" == false ]]; then
    echo "✓ 没有需要修复的内容"
    exit 0
fi

if [[ "$APPLY" != true ]]; then
    echo
    echo "以上为体检结果，未做任何修改。确认无误后加 --apply 执行。"
    exit 0
fi

# ---- 3. 执行 ---------------------------------------------------------
if [[ ${#orphans[@]} -gt 0 ]]; then
    # 拼 IN 列表；path 是程序生成的 UUID 文件名，不含引号，直接拼接是安全的
    in_list=$(printf "'%s'," "${orphans[@]}"); in_list="${in_list%,}"
    mysql_q -e "DELETE FROM resource WHERE path IN ($in_list)"
    echo "✓ 已删除 ${#orphans[@]} 条死指针（只删 DB 记录，文件本来就不存在）"
fi

if [[ "$logo_broken" == true ]]; then
    # 换成随前端一起打包的 PWA 图标：自托管、必然存在、本身就是站点图标
    mysql_q -e "UPDATE site_config SET config_value = '/pwa-192x192.png' WHERE config_key = 'site_logo'"
    echo "✓ 站点 logo 已改为 /pwa-192x192.png（可在后台 → 设置 → 基础信息 换成自己的图）"
fi

echo "完成。若站点配置有缓存，重启后端或在后台保存一次设置即可刷新。"
