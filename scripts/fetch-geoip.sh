#!/usr/bin/env bash
# ============================================================
# 下载 / 更新 MaxMind GeoLite2-City 离线库
#
# 为什么不入 git：66MB 二进制、且 MaxMind 每月更新，
# 提交进版本库会让仓库历史无限膨胀。
#
# 用法：
#   MAXMIND_LICENSE_KEY=xxx bash scripts/fetch-geoip.sh
#
# 免费 license key 申请：https://www.maxmind.com/en/geolite2/signup
# ============================================================
set -euo pipefail

DEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/data/geoip"
DEST="$DEST_DIR/GeoLite2-City.mmdb"

mkdir -p "$DEST_DIR"

if [ -z "${MAXMIND_LICENSE_KEY:-}" ]; then
    echo "错误：需要设置 MAXMIND_LICENSE_KEY" >&2
    echo "免费申请：https://www.maxmind.com/en/geolite2/signup" >&2
    echo >&2
    echo "已有 .mmdb 文件的话，直接放到：$DEST" >&2
    exit 1
fi

URL="https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=${MAXMIND_LICENSE_KEY}&suffix=tar.gz"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

echo "下载 GeoLite2-City..."
curl -fsSL "$URL" -o "$TMP/geoip.tar.gz"

echo "解包..."
tar -xzf "$TMP/geoip.tar.gz" -C "$TMP"
FOUND="$(find "$TMP" -name '*.mmdb' -print -quit)"
if [ -z "$FOUND" ]; then
    echo "错误：压缩包中未找到 .mmdb 文件" >&2
    exit 1
fi

mv "$FOUND" "$DEST"
echo "完成：$DEST ($(du -h "$DEST" | cut -f1))"
echo "重启后端生效：docker compose restart backend"
