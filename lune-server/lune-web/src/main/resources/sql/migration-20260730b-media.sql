-- ============================================================
-- 迁移：第三方随机图/音乐外链 → 自托管精选资源
-- 日期：2026-07-30
--
-- 背景：
--   1. home_music_list 里的 jsdelivr 音乐链接全部 404（imsyy/file 仓库已无音乐文件），
--      这是「首页音乐播放不了」的真正原因；NetEase 外链会 302 到 http:// 地址，
--      又会被 nginx CSP `media-src 'self' https:` 拦下，因此必须自托管。
--   2. 所有背景图都指向 t.alcy.cc 随机 API：每次刷新画面都变、常出现角色特写与高饱和构图，
--      既是运行时外部依赖，也直接影响前端美感。改为自托管精选风景图。
--
-- 资源位置：lune-ui/public/media/{music,bg}/，随 nginx 镜像部署，无需同步 volume。
-- 版权说明：lune-ui/public/media/CREDITS.md（CC BY 曲目的 license 字段必须保留）。
--
-- 幂等性：本文件每次 deploy 都会重跑，因此只覆盖「仍指向失效第三方地址」的值，
--         绝不覆盖用户后台自行配置过的背景/歌单。
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 1. 歌单：仅当仍是失效的 jsdelivr 外链时替换 ----------
INSERT INTO site_config (config_key, config_value, config_type, description) VALUES
('home_music_list', '[{"name":"Clair de Lune","artist":"Claude Debussy","url":"/media/music/lune-clair-de-lune.mp3","cover":"/media/bg/lune-bg-night-lake.webp","lrc":"月光倾泻，如水漫过夜色","license":"CC BY 3.0 · Wikimedia Commons"},{"name":"Reverie","artist":"Scott Buckley","url":"/media/music/lune-reverie.mp3","cover":"/media/bg/lune-bg-starry-tree.webp","lrc":"一场温柔的白日梦","license":"CC BY 4.0 · Scott Buckley"},{"name":"Gymnopédie No.3","artist":"Erik Satie","url":"/media/music/lune-gymnopedie-no3.mp3","cover":"/media/bg/lune-bg-grass-field.webp","lrc":"缓慢而克制的忧郁","license":"Public domain"},{"name":"Nocturne Op.27","artist":"Frédéric Chopin","url":"/media/music/lune-nocturne-op27.mp3","cover":"/media/bg/lune-bg-valley-dusk.webp","lrc":"夜曲，写给无人的夜","license":"Public domain"},{"name":"Nocturne Op.62","artist":"Frédéric Chopin","url":"/media/music/lune-nocturne-op62.mp3","cover":"/media/bg/lune-bg-water-door.webp","lrc":"最后的夜曲，最长的温柔","license":"Public domain"}]', 'public', '首页音乐歌单（自托管，含 license 署名字段）')
ON DUPLICATE KEY UPDATE
  config_value = IF(
    config_value IS NULL OR config_value = '' OR config_value LIKE '%jsdelivr%' OR config_value LIKE '%music.163.com%',
    VALUES(config_value), config_value);

-- ---------- 2. 背景图：仅当仍是 t.alcy.cc 随机 API 时替换 ----------
INSERT INTO site_config (config_key, config_value, config_type, description) VALUES
('landing_bg',          '["/media/bg/lune-bg-valley-dusk.webp","/media/bg/lune-bg-starry-tree.webp"]', 'public', 'Landing 页背景'),
('home_hero_bg',        '["/media/bg/lune-bg-sky-rooftop.webp","/media/bg/lune-bg-grass-field.webp"]', 'public', '首页顶部 Banner'),
('home_content_bg',     '["/media/bg/lune-bg-water-door.webp"]',                                        'public', '首页内容区'),
('family_hero_bg',      '["/media/bg/lune-bg-grass-field.webp","/media/bg/lune-bg-green-bridge.webp"]', 'public', '家页顶部 Banner'),
('family_content_bg',   '["/media/bg/lune-bg-water-door.webp"]',                                        'public', '家页内容区'),
('treehole_danmaku_bg', '["/media/bg/lune-bg-night-lake.webp","/media/bg/lune-bg-starry-tree.webp"]',   'public', '树洞弹幕区'),
('treehole_content_bg', '["/media/bg/lune-bg-night-lake.webp"]',                                        'public', '树洞时间线'),
('essay_hero_bg',       '["/media/bg/lune-bg-street.webp","/media/bg/lune-bg-sky-rooftop.webp"]',       'public', '随笔顶部 Banner'),
('essay_content_bg',    '["/media/bg/lune-bg-water-door.webp"]',                                        'public', '随笔内容区'),
('record_hero_bg',      '["/media/bg/lune-bg-green-bridge.webp","/media/bg/lune-bg-street.webp"]',      'public', '记录顶部 Banner'),
('record_content_bg',   '["/media/bg/lune-bg-water-door.webp"]',                                        'public', '记录内容区'),
('wish_hero_bg',        '["/media/bg/lune-bg-sky-rooftop.webp","/media/bg/lune-bg-valley-dusk.webp"]',  'public', '许愿池顶部 Banner'),
('wish_content_bg',     '["/media/bg/lune-bg-grass-field.webp"]',                                       'public', '许愿池内容区'),
('resume_hero_bg',      '["/media/bg/lune-bg-green-bridge.webp","/media/bg/lune-bg-valley-dusk.webp"]', 'public', '简历页顶部 Banner')
ON DUPLICATE KEY UPDATE
  config_value = IF(
    config_value IS NULL OR config_value = '' OR config_value LIKE '%alcy.cc%' OR config_value LIKE '%dmoe.cc%',
    VALUES(config_value), config_value);

-- ---------- 3. 移动端竖屏背景图 ----------
-- 这 14 个 *_bg_mobile key 前端（stores/app.js + usePageBackground）一直在读，
-- 而库里存的还是 t.alcy.cc 随机 API：横版 PC 图已换成自托管，手机端反而留在
-- 第三方随机图上 —— 而移动端分支优先级更高，等于手机用户永远看不到精选图。
-- 这里换成同一批图的 9:16 竖裁版（见 media/CREDITS.md）：构图中心不被 cover 裁掉，
-- 体积约为横版的 30%。取值与 PC 端一一对应，保持两端观感一致。
INSERT INTO site_config (config_key, config_value, config_type, description) VALUES
('landing_bg_mobile',          '["/media/bg/lune-bg-valley-dusk-m.webp","/media/bg/lune-bg-starry-tree-m.webp"]', 'public', 'Landing 页背景（移动端竖屏）'),
('home_hero_bg_mobile',        '["/media/bg/lune-bg-sky-rooftop-m.webp","/media/bg/lune-bg-grass-field-m.webp"]', 'public', '首页顶部 Banner（移动端竖屏）'),
('home_content_bg_mobile',     '["/media/bg/lune-bg-water-door-m.webp"]',                                          'public', '首页内容区（移动端竖屏）'),
('family_hero_bg_mobile',      '["/media/bg/lune-bg-grass-field-m.webp","/media/bg/lune-bg-green-bridge-m.webp"]', 'public', '家页顶部 Banner（移动端竖屏）'),
('family_content_bg_mobile',   '["/media/bg/lune-bg-water-door-m.webp"]',                                          'public', '家页内容区（移动端竖屏）'),
('treehole_danmaku_bg_mobile', '["/media/bg/lune-bg-night-lake-m.webp","/media/bg/lune-bg-starry-tree-m.webp"]',   'public', '树洞弹幕区（移动端竖屏）'),
('treehole_content_bg_mobile', '["/media/bg/lune-bg-night-lake-m.webp"]',                                          'public', '树洞时间线（移动端竖屏）'),
('essay_hero_bg_mobile',       '["/media/bg/lune-bg-street-m.webp","/media/bg/lune-bg-sky-rooftop-m.webp"]',       'public', '随笔顶部 Banner（移动端竖屏）'),
('essay_content_bg_mobile',    '["/media/bg/lune-bg-water-door-m.webp"]',                                          'public', '随笔内容区（移动端竖屏）'),
('record_hero_bg_mobile',      '["/media/bg/lune-bg-green-bridge-m.webp","/media/bg/lune-bg-street-m.webp"]',      'public', '记录顶部 Banner（移动端竖屏）'),
('record_content_bg_mobile',   '["/media/bg/lune-bg-water-door-m.webp"]',                                          'public', '记录内容区（移动端竖屏）'),
('wish_hero_bg_mobile',        '["/media/bg/lune-bg-sky-rooftop-m.webp","/media/bg/lune-bg-valley-dusk-m.webp"]',  'public', '许愿池顶部 Banner（移动端竖屏）'),
('wish_content_bg_mobile',     '["/media/bg/lune-bg-grass-field-m.webp"]',                                         'public', '许愿池内容区（移动端竖屏）'),
('resume_hero_bg_mobile',      '["/media/bg/lune-bg-green-bridge-m.webp","/media/bg/lune-bg-valley-dusk-m.webp"]', 'public', '简历页顶部 Banner（移动端竖屏）')
ON DUPLICATE KEY UPDATE
  config_value = IF(
    config_value IS NULL OR config_value = '' OR config_value = '[]'
      OR config_value LIKE '%alcy.cc%' OR config_value LIKE '%dmoe.cc%',
    VALUES(config_value), config_value);
