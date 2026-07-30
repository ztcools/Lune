#!/bin/bash
# ============================================================
# Lune 自托管字体生成脚本
#
# 下载 Latin 展示字体（Fredoka / Comfortaa / Quicksand / Caveat）的
# latin 分片到 lune-ui/public/assets/fonts/，并生成配套 fonts.css。
#
# 用法:
#   bash docker/fonts/download-fonts.sh
#
# 需要能访问 fonts.googleapis.com（大陆需代理）。产物是 12 个 woff2
# 约 344 KB，直接提交进仓库 —— 生产环境不再依赖任何字体 CDN。
#
# 只处理 Latin：中文正文用系统字体，三款中文书法体走国内镜像并由
# lune-ui/src/utils/loadFonts.js 延迟注入。原因详见 lune-ui/index.html。
# 要改字体清单请编辑同目录的 build-fonts.js。
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FONTS_DIR="$(cd "$SCRIPT_DIR/../../lune-ui/public/assets" && pwd)/fonts"

log_info() { echo -e "\033[34m[INFO]\033[0m  $*"; }
log_ok()   { echo -e "\033[32m[OK]\033[0m    $*"; }
log_err()  { echo -e "\033[31m[ERROR]\033[0m $*"; }

command -v node >/dev/null 2>&1 || { log_err "需要 node"; exit 1; }

if ! curl -sf -o /dev/null --max-time 10 \
     "https://fonts.googleapis.com/css2?family=Fredoka:wght@400&display=swap"; then
  log_err "无法访问 fonts.googleapis.com（大陆需代理）"
  exit 1
fi

log_info "生成自托管字体到 $FONTS_DIR"
mkdir -p "$FONTS_DIR"
# 先清掉旧产物，避免改字体清单后残留孤儿 woff2 被打进构建
rm -f "$FONTS_DIR"/*.woff2 "$FONTS_DIR"/fonts.css

node "$SCRIPT_DIR/build-fonts.js" "$FONTS_DIR"

log_ok "完成，共 $(ls -1 "$FONTS_DIR"/*.woff2 | wc -l) 个字体文件"
log_info "记得提交 lune-ui/public/assets/fonts/ 下的产物"
