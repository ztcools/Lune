# 自托管媒体资源版权说明

本目录下的音频与背景图均为自托管资源，随 nginx 镜像一同部署，不依赖任何第三方 CDN。
选择自托管的原因：原先配置的第三方音乐 URL 已全部 404，且 NetEase 外链会 302 到 `http://`
地址，被 nginx CSP `media-src 'self' https:` 拦截。

## 音乐 `media/music/`

| 文件 | 曲目 | 演奏/作者 | 许可 | 需署名 |
|------|------|-----------|------|--------|
| `lune-clair-de-lune.mp3` | Clair de Lune (Suite bergamasque) | Claude Debussy | **CC BY 3.0** | ✅ 是 |
| `lune-gymnopedie-no3.mp3` | Gymnopédie No. 3 | Erik Satie / Michael Laucke | Public domain | 否 |
| `lune-nocturne-op27.mp3` | Nocturne Op. 27 | Frédéric Chopin | Public domain | 否 |
| `lune-nocturne-op62.mp3` | Nocturne Op. 62 | Frédéric Chopin / Olga Gurevich | Public domain | 否 |
| `lune-reverie.mp3` | Reverie | Scott Buckley | **CC BY 4.0** | ✅ 是 |

来源：Wikimedia Commons（许可已通过 `prop=imageinfo&iiprop=extmetadata` 逐个核验）。
处理：`ffmpeg -af loudnorm=I=-16:TP=-1.5:LRA=11 -c:a libmp3lame -b:a 128k -ar 44100`
（统一响度，避免切歌音量跳变）。

> 刻意排除了一版 CC BY-SA 3.0 的《月光奏鸣曲》录音——转码 MP3 可被视为演绎作品，
> 会触发 share-alike 传染性条款。

**署名实现**：`MusicPlayer.vue` 在曲目信息区渲染 `currentSong.license` 字段，
歌单数据存于 `site_config.home_music_list`，字段含 `{name, artist, url, cover, lrc, license}`。
CC BY 曲目的 `license` 必须保留，否则不符合许可要求。

## 背景图 `media/bg/`

来源：`t.alcy.cc/fj`（二次元风景 API）随机抽取 10 张候选后人工筛选 8 张。
筛选标准：柔和、低对比、无人物主体、不与站点绿色主题冲突、不干扰前景可读性
（明确弃用了角色特写与高饱和构图——正是用户提示的「影响前端美感」风险）。

处理：`sharp().resize(1920x1200, fit: inside).webp({ quality: 76 })`，8 张共 816 KB。

| 文件 | 画面 | 用途 |
|------|------|------|
| `lune-bg-sky-rooftop.webp` | 淡蓝天空/云/飞鸟 | 首页 hero |
| `lune-bg-grass-field.webp` | 草原蓝天 | 呼应绿色主题 |
| `lune-bg-water-door.webp` | 白墙 + 碧水 | 内容区（极柔和） |
| `lune-bg-green-bridge.webp` | 绿意石桥流水 | 自然主题 |
| `lune-bg-valley-dusk.webp` | 蓝调山谷暮色 | Landing |
| `lune-bg-night-lake.webp` | 青绿夜湖 | 树洞 |
| `lune-bg-street.webp` | 日式街道 | 随笔 / 记录 |
| `lune-bg-starry-tree.webp` | 星空孤树 | Landing 备选 |

### 移动端竖屏版 `*-m.webp`

同一批图按 **9:16 居中裁切**（宽 = 高 × 0.5625）另存一份，用于 `*_bg_mobile` 配置项。
横图直接铺到竖屏上时，`background-size: cover` 会按高度放大、把两侧裁掉，
主体（人物、桥拱、街道纵深）往往被切出画面；竖裁版在裁切阶段就保住了构图中心，
且体积只有横版的 ~30%（8 张共 272 KB），省的是手机流量。

处理：`cwebp -q 80 -crop <x> 0 <w> <h>`，`x` 取居中偏移。裁切不改变授权，
版权与上表一致（同源同许可）。

| 文件 | 尺寸 | 对应横版 |
|------|------|----------|
| `lune-bg-sky-rooftop-m.webp` | 608×1080 | `lune-bg-sky-rooftop.webp` |
| `lune-bg-green-bridge-m.webp` | 608×1080 | `lune-bg-green-bridge.webp` |
| `lune-bg-starry-tree-m.webp` | 608×1080 | `lune-bg-starry-tree.webp` |
| `lune-bg-street-m.webp` | 608×1080 | `lune-bg-street.webp` |
| `lune-bg-valley-dusk-m.webp` | 608×1080 | `lune-bg-valley-dusk.webp` |
| `lune-bg-night-lake-m.webp` | 456×810 | `lune-bg-night-lake.webp` |
| `lune-bg-water-door-m.webp` | 450×800 | `lune-bg-water-door.webp` |
| `lune-bg-grass-field-m.webp` | 394×700 | `lune-bg-grass-field.webp` |
