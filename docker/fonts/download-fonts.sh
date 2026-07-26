#!/bin/bash
# ============================================================
# Lune 字体下载脚本
# 从 Google Fonts 下载 woff2 字体文件到本地
#
# 用法: bash download-fonts.sh
#
# 如果无法访问 Google，可以使用镜像：
#   https://fonts.googleapis.cn  （国内镜像）
#   或使用代理
# ============================================================
set -euo pipefail

FONTS_DIR="$(cd "$(dirname "$0")/../../lune-ui/public/assets/fonts" && pwd)"
mkdir -p "$FONTS_DIR"

# Google Fonts 国内镜像（可选）
# BASE_URL="https://fonts.googleapis.cn"
BASE_URL="https://fonts.googleapis.com"

log_info() { echo -e "\033[34m[INFO]\033[0m  $*"; }
log_ok()   { echo -e "\033[32m[OK]\033[0m    $*"; }
log_err()  { echo -e "\033[31m[ERROR]\033[0m $*"; }

# 构造 Google Fonts CSS URL（Latin 字体只需要 latin 子集）
FONTS_CSS_URL="${BASE_URL}/css2?family=Fredoka:wght@400;500;600;700&family=Comfortaa:wght@400;500;600;700&family=Quicksand:wght@400;500;600;700&display=swap"

log_info "下载 Latin 字体 CSS..."
CSS_CONTENT=$(curl -sL -H "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36" "$FONTS_CSS_URL" 2>/dev/null || true)

if [ -z "$CSS_CONTENT" ]; then
    log_err "无法连接到 Google Fonts。请尝试："
    log_err "  1. 使用代理: export https_proxy=http://your-proxy:port"
    log_err "  2. 手动下载字体文件放到: $FONTS_DIR"
    log_err "  3. 使用国内镜像: 修改脚本中的 BASE_URL 为 fonts.googleapis.cn"
    exit 1
fi

# 提取 woff2 URL 并下载
echo "$CSS_CONTENT" | grep -oP 'url\((https://[^)]+\.woff2)\)' | sed 's/url(//;s/)//' | sort -u | while read -r url; do
    filename=$(basename "$url" | cut -d'?' -f1)
    log_info "下载: $filename"
    curl -sL -o "$FONTS_DIR/$filename" "$url"
    log_ok "$filename 下载完成"
done

# 复制 fonts.css 到 fonts 目录
cp "$(dirname "$0")/fonts.css" "$FONTS_DIR/fonts.css"

log_ok "所有字体下载完成！"
log_info "字体文件位于: $FONTS_DIR"
echo ""
log_info "注意: Noto Sans SC 和 ZCOOL XiaoWei 中文字体体积较大，"
log_info "已使用系统默认中文字体作为回退。如需自托管中文字体，"
log_info "请手动下载并添加 @font-face 声明到 fonts.css。"
