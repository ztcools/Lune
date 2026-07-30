-- =====================================================================
-- 迁移：把演示数据里残留的第三方图床地址换成自托管资源
-- 日期：2026-07-30
-- 幂等：每条 UPDATE 都带 LIKE '%unsplash%' 前置条件，重复执行是空操作
-- =====================================================================
--
-- 背景：
-- 生产库 2026-07-27 首次初始化时带进来一批演示内容，图片全部指向
-- images.unsplash.com（文章封面 5 张、家页 4 张、随笔 7 张、记录 8 张）。仓库
-- 当前的种子代码已经不产生这些地址了，但**已经落库的行不会自己变**，只能用数据
-- 迁移修。本地 dev 库同样中招，所以这个文件对两个环境都适用。
--
-- 为什么必须换掉：
-- 1) 站点面向国内访问，境外图床在访客浏览器侧不可靠 —— 服务器自己 curl 得通
--    （实测 0.26s）不代表访客加载得出来，这两件事很容易被混为一谈；
-- 2) 项目已经把 8 张二次元风景图打进 nginx 镜像（/media/bg/*.webp，共 816KB），
--    自托管资源就在手边，没有理由继续依赖外链。
--
-- 换成什么：/media/bg/ 下已有的 8 张图轮换使用，保证同一条记录内不重复。
-- 这些都是**演示内容的占位图**，作者随时可以在后台替换；家页的情侣头像也用了风景
-- 图（这批素材刻意没有人物特写，理由见 lune-ui/public/media/CREDITS.md），
-- 上线后建议手动换成真实合照。
--
-- 注意：站点 logo 失效、resource 表死指针这两个问题**不在这个文件里**——它们取决于
-- 某台机器的 upload 卷里到底有没有那个文件（生产缺、本地有），用 SQL 无条件改会
-- 误伤。见根目录 prune-orphan-resources.sh。

-- ---------------------------------------------------------------------
-- 1. 文章封面
-- ---------------------------------------------------------------------
UPDATE article SET cover = '/media/bg/lune-bg-sky-rooftop.webp'  WHERE id = 5 AND cover LIKE '%unsplash%';
UPDATE article SET cover = '/media/bg/lune-bg-grass-field.webp'  WHERE id = 6 AND cover LIKE '%unsplash%';
UPDATE article SET cover = '/media/bg/lune-bg-water-door.webp'   WHERE id = 7 AND cover LIKE '%unsplash%';
UPDATE article SET cover = '/media/bg/lune-bg-night-lake.webp'   WHERE id = 8 AND cover LIKE '%unsplash%';
UPDATE article SET cover = '/media/bg/lune-bg-starry-tree.webp'  WHERE id = 9 AND cover LIKE '%unsplash%';

-- 兜底：万一还有别的文章封面指向外链（例如后续又导入演示数据）
UPDATE article SET cover = '/media/bg/lune-bg-valley-dusk.webp' WHERE cover LIKE '%unsplash%';

-- ---------------------------------------------------------------------
-- 2. 家页（封面 / 背景 / 双方头像）
-- ---------------------------------------------------------------------
UPDATE family
SET cover       = CASE WHEN cover       LIKE '%unsplash%' THEN '/media/bg/lune-bg-green-bridge.webp' ELSE cover       END,
    bg_cover    = CASE WHEN bg_cover    LIKE '%unsplash%' THEN '/media/bg/lune-bg-street.webp'       ELSE bg_cover    END,
    man_cover   = CASE WHEN man_cover   LIKE '%unsplash%' THEN '/media/bg/lune-bg-night-lake.webp'   ELSE man_cover   END,
    woman_cover = CASE WHEN woman_cover LIKE '%unsplash%' THEN '/media/bg/lune-bg-valley-dusk.webp'  ELSE woman_cover END
WHERE CONCAT_WS('', cover, bg_cover, man_cover, woman_cover) LIKE '%unsplash%';

-- ---------------------------------------------------------------------
-- 3. 随笔九宫格（JSON 数组，逐个下标替换，避免一条里三张图变成同一张）
-- ---------------------------------------------------------------------
UPDATE essay SET media = JSON_SET(media, '$[0].url', '/media/bg/lune-bg-water-door.webp')
WHERE id = 7 AND media LIKE '%unsplash%';

UPDATE essay SET media = JSON_SET(media,
    '$[0].url', '/media/bg/lune-bg-starry-tree.webp',
    '$[1].url', '/media/bg/lune-bg-green-bridge.webp',
    '$[2].url', '/media/bg/lune-bg-grass-field.webp')
WHERE id = 8 AND media LIKE '%unsplash%';

UPDATE essay SET media = JSON_SET(media,
    '$[0].url', '/media/bg/lune-bg-street.webp',
    '$[1].url', '/media/bg/lune-bg-valley-dusk.webp')
WHERE id = 9 AND media LIKE '%unsplash%';

UPDATE essay SET media = JSON_SET(media, '$[0].url', '/media/bg/lune-bg-sky-rooftop.webp')
WHERE id = 10 AND media LIKE '%unsplash%';

-- ---------------------------------------------------------------------
-- 4. 记录卡片媒体
-- ---------------------------------------------------------------------
UPDATE record SET media = JSON_SET(media,
    '$[0].url', '/media/bg/lune-bg-grass-field.webp',
    '$[1].url', '/media/bg/lune-bg-night-lake.webp',
    '$[2].url', '/media/bg/lune-bg-water-door.webp')
WHERE id = 9 AND media LIKE '%unsplash%';

UPDATE record SET media = JSON_SET(media,
    '$[0].url', '/media/bg/lune-bg-starry-tree.webp',
    '$[1].url', '/media/bg/lune-bg-sky-rooftop.webp')
WHERE id = 10 AND media LIKE '%unsplash%';

UPDATE record SET media = JSON_SET(media, '$[0].url', '/media/bg/lune-bg-green-bridge.webp')
WHERE id = 11 AND media LIKE '%unsplash%';

UPDATE record SET media = JSON_SET(media,
    '$[0].url', '/media/bg/lune-bg-valley-dusk.webp',
    '$[1].url', '/media/bg/lune-bg-street.webp')
WHERE id = 12 AND media LIKE '%unsplash%';

-- 记录卡片还有独立的 cover 字段
UPDATE record SET cover = '/media/bg/lune-bg-water-door.webp' WHERE cover LIKE '%unsplash%';
