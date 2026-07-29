-- ============================================================
-- Lune 全套演示数据填充
-- 覆盖：文章/标签/随笔/记录/树洞/评论/日记/家页/许愿/点赞/背景图
-- 幂等可重入（按标志性字段判断是否已存在，可安全重复执行）
-- 执行：docker exec -i lune-mysql-dev mysql -uroot -p<PWD> lune < seed-data.sql
-- 图片使用 picsum.photos 固定种子占位图，可在后台一键替换为真实图片
-- ============================================================

-- ============ 标签 ============
INSERT INTO `tag` (`name`, `color`) SELECT 'Vue3', '#42b883' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = 'Vue3');
INSERT INTO `tag` (`name`, `color`) SELECT 'Spring Boot', '#6db33f' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = 'Spring Boot');
INSERT INTO `tag` (`name`, `color`) SELECT 'Docker', '#2496ed' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = 'Docker');
INSERT INTO `tag` (`name`, `color`) SELECT '生活随笔', '#ff7d54' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = '生活随笔');
INSERT INTO `tag` (`name`, `color`) SELECT '前端', '#61dafb' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = '前端');
INSERT INTO `tag` (`name`, `color`) SELECT '摄影', '#9b59b6' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = '摄影');
INSERT INTO `tag` (`name`, `color`) SELECT '效率', '#f39c12' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = '效率');
INSERT INTO `tag` (`name`, `color`) SELECT '随笔感悟', '#1abc9c' WHERE NOT EXISTS (SELECT 1 FROM `tag` WHERE `name` = '随笔感悟');

-- ============ 文章 ============
INSERT INTO `article` (`user_id`, `category_id`, `title`, `summary`, `content`, `cover`, `view_count`, `like_count`, `status`, `is_top`, `create_time`)
SELECT 1, 1, '从零搭建一个全栈个人博客：技术选型与架构思考',
 '记录我用 Vue3 + Spring Boot + MySQL + Redis + Docker 从零搭建个人博客 Lune 的完整过程，包含技术选型背后的思考。',
 '# 从零搭建一个全栈个人博客\n\n> 一直想拥有一个完全属于自己的小站，记录生活、安放想法。这个念头在心里搁置了很久，直到最近才真正动手，把它从草图变成了现实。\n\n## 为什么是自建而不是用现成平台\n\n市面上的博客平台很多，WordPress、Hexo、语雀……但或多或少都有些不尽如人意：要么太重，要么定制性太差，要么数据不在自己手里。我想要的是一个**完全可控、足够轻、又能承载各种玩法**的空间，于是决定自己动手。\n\n## 技术选型\n\n| 层级 | 技术 | 选择理由 |\n|------|------|----------|\n| 前端 | Vue 3 + Vite | 组合式 API 写起来顺手，Vite 秒级热更新 |\n| UI | Element Plus | 后台管理开箱即用，主题可定制 |\n| 后端 | Spring Boot 3 | 生态成熟，Java 17 语法糖很香 |\n| 数据库 | MySQL 8 | 稳定可靠，utf8mb4 全表情支持 |\n| 缓存 | Redis 7 | 会话黑名单、计数、限流都离不开它 |\n| 部署 | Docker Compose | 一条命令拉起整套环境 |\n\n## 架构上的小决定\n\n整个系统采用前后端分离，Nginx 做反向代理和静态资源服务。后端按 `Controller → Service → Mapper` 分层，统一返回 `Result<T>` 包装。\n\n```java\n@GetMapping(\"/{id}\")\npublic Result<Article> detail(@PathVariable Long id) {\n    return Result.ok(articleService.getById(id));\n}\n```\n\n## 踩过的坑\n\n1. **软删除与唯一索引冲突**：逻辑删除的字段参与唯一约束时，需要把 `deleted` 也放进联合索引。\n2. **计数并发**：浏览量、点赞用原子 SQL 更新，避免并发下丢失。\n3. **富文本 XSS**：所有用户输入在入库前统一转义，展示层再做一次白名单过滤。\n\n## 写在最后\n\n建这个博客的过程，本身就是最好的学习。它不只是一个网站，更像是一个可以一直打磨的作品。后续我会继续在这个系列里分享评论系统、弹幕树洞、许愿池等模块的实现细节。',
 'https://picsum.photos/seed/lune-a1/800/450', 328, 42, 1, 1, '2026-07-10 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '从零搭建一个全栈个人博客：技术选型与架构思考');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `summary`, `content`, `cover`, `view_count`, `like_count`, `status`, `is_top`, `create_time`)
SELECT 1, 1, 'Vue3 组合式 API 实战：把 800 行组件拆成可维护的 composables',
 '一个真实组件的重构记录：如何通过抽取 composables，把一个臃肿的单文件组件拆得清晰可测。',
 '# Vue3 组合式 API 实战\n\n> 当一个组件超过 500 行，它就不再是组件，而是一团需要被拆解的逻辑。\n\n## 问题现场\n\n项目里的文章阅读页 `ArticleReader.vue` 一度膨胀到 850 行：Markdown 渲染、目录生成、阅读进度、点赞收藏、评论加载全揉在一起。改一处动全身，测试也无从下手。\n\n## 拆解思路\n\n按**关注点**把逻辑抽成 composables：\n\n```js\n// useReadingProgress.js —— 阅读进度\nexport function useReadingProgress(elRef) {\n  const progress = ref(0)\n  const onScroll = () => {\n    const el = elRef.value\n    if (!el) return\n    const { scrollTop, scrollHeight, clientHeight } = el\n    progress.value = Math.min(100, (scrollTop / (scrollHeight - clientHeight)) * 100)\n  }\n  onMounted(() => elRef.value?.addEventListener(\'scroll\', onScroll))\n  onUnmounted(() => elRef.value?.removeEventListener(\'scroll\', onScroll))\n  return { progress }\n}\n```\n\n类似的还有 `useToc`（目录）、`useArticleLike`（点赞）、`useCommentList`（评论分页）。\n\n## 收益\n\n- 组件从 850 行降到 200 行，只负责模板编排\n- 每个 composable 可独立单测\n- 逻辑在多个页面间复用\n\n## 几个原则\n\n1. **单一职责**：一个 composable 只做一件事\n2. **依赖注入参数化**：DOM 引用、配置都从外部传入\n3. **副作用自清理**：`onUnmounted` 里务必移除监听\n\n组合式 API 的真正威力，在于让逻辑像积木一样被自由组合和复用。',
 'https://picsum.photos/seed/lune-a2/800/450', 256, 35, 1, 0, '2026-07-14 14:30:00'
WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = 'Vue3 组合式 API 实战：把 800 行组件拆成可维护的 composables');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `summary`, `content`, `cover`, `view_count`, `like_count`, `status`, `is_top`, `create_time`)
SELECT 1, 1, 'Docker Compose 部署全栈应用：我的生产配置清单',
 '从开发到生产，一份可直接抄作业的 Docker Compose 部署清单，含健康检查、日志轮转、资源限制。',
 '# Docker Compose 部署全栈应用\n\n> 把本地能跑的东西搬到服务器上不翻车，靠的不是运气，而是一份靠谱的配置清单。\n\n## 整体拓扑\n\n```\n用户 → Nginx(80) → ┬→ 静态资源(前端构建产物)\n                    └→ /api → Spring Boot(8081) → ┬→ MySQL\n                                                   └→ Redis\n```\n\n## 关键配置点\n\n**1. 健康检查**：后端必须暴露健康端点，Nginx 依赖它再启动。\n\n```yaml\nhealthcheck:\n  test: [\"CMD\", \"curl\", \"-f\", \"http://localhost:8081/api/actuator/health\"]\n  interval: 30s\n  start_period: 90s\n```\n\n**2. 日志轮转**：容器日志不设限早晚把磁盘撑爆。\n\n```yaml\nlogging:\n  driver: \"json-file\"\n  options:\n    max-size: \"10m\"\n    max-file: \"3\"\n```\n\n**3. 数据持久化**：数据库和上传文件用 named volume，别用容器内路径。\n\n**4. 密钥外置**：所有密码、密钥走环境变量，绝不写进镜像和仓库。\n\n## 资源限制（2C4G 小服务器）\n\n小内存机器上，MySQL 是最吃资源的，需要给它"瘦身"：\n\n```yaml\ndeploy:\n  resources:\n    limits:\n      memory: 768M\n```\n\n同时把 `innodb_buffer_pool_size` 降到 256M，JVM 堆限制在 512M。\n\n## 备份策略\n\n每天凌晨 3 点 `mysqldump` 导出并压缩，保留最近 7 天，异地再同步一份。数据无价，备份是底线。',
 'https://picsum.photos/seed/lune-a3/800/450', 412, 58, 1, 0, '2026-07-18 10:20:00'
WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = 'Docker Compose 部署全栈应用：我的生产配置清单');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `summary`, `content`, `cover`, `view_count`, `like_count`, `status`, `is_top`, `create_time`)
SELECT 1, 2, '把日子过成诗：为什么我开始认真记录生活',
 '不是写给谁看，而是写给未来的自己。关于记录、记忆与热爱的一点感悟。',
 '# 把日子过成诗\n\n> 我们总以为难忘的事会永远记得，其实记忆是最不可靠的东西。\n\n## 为什么开始记录\n\n去年翻看几年前的照片，突然发现很多当时以为"这辈子都忘不了"的瞬间，已经模糊得只剩一个轮廓。那一刻意识到：**不记录，就会遗忘。**\n\n于是有了这个博客。它不追求流量，不讨好算法，只是安安静静地，把每一个值得记住的时刻收好。\n\n## 记录改变了什么\n\n开始记录之后，我发现自己变得更"敏感"了——\n\n- 会为一朵云的形状停下脚步\n- 会把偶然听到的一句话记进备忘录\n- 会在平淡的日子里，主动去制造一些小小的仪式感\n\n记录让我们从"经过生活"变成"活在生活里"。\n\n## 写给未来的自己\n\n很多年后，当我回头翻这些文字和图片，希望会感谢现在这个愿意花时间记录的人。\n\n就像博客里写的那句话：*时刻保持思考，永远热爱生活。*\n\n愿我们都能把平凡的日子，过成自己喜欢的样子。',
 'https://picsum.photos/seed/lune-a4/800/450', 189, 67, 1, 0, '2026-07-22 20:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '把日子过成诗：为什么我开始认真记录生活');

INSERT INTO `article` (`user_id`, `category_id`, `title`, `summary`, `content`, `cover`, `view_count`, `like_count`, `status`, `is_top`, `create_time`)
SELECT 1, 2, '我的极简工作流：工具不在多，在于顺手',
 '分享我日常开发和生活中真正高频使用的几件工具，以及背后的取舍逻辑。',
 '# 我的极简工作流\n\n> 工具是为人服务的，不是反过来。折腾工具的尽头，是回归简单。\n\n## 开发\n\n- **编辑器**：VS Code，插件只留必要的五六个\n- **终端**：zsh + 一套自己调教过的别名\n- **笔记**：Markdown 纯文本，Git 管理，永远不丢\n\n## 效率\n\n我删掉了手机上大部分 App，只留真正提升生活质量的。专注力是最稀缺的资源，任何偷走注意力的东西都要警惕。\n\n## 一个原则\n\n**够用就好，顺手最重要。** 不追新、不折腾，把时间留给真正创造价值的事。\n\n工具的温度，来自使用它的人对生活的热爱。',
 'https://picsum.photos/seed/lune-a5/800/450', 143, 29, 1, 0, '2026-07-25 16:40:00'
WHERE NOT EXISTS (SELECT 1 FROM `article` WHERE `title` = '我的极简工作流：工具不在多，在于顺手');

-- 文章标签关联
INSERT INTO `article_tag` (`article_id`, `tag_id`)
SELECT a.id, t.id FROM `article` a JOIN `tag` t ON t.name = 'Vue3'
WHERE a.title LIKE 'Vue3 组合式%' AND NOT EXISTS (SELECT 1 FROM article_tag at WHERE at.article_id = a.id AND at.tag_id = t.id);
INSERT INTO `article_tag` (`article_id`, `tag_id`)
SELECT a.id, t.id FROM `article` a JOIN `tag` t ON t.name = '前端'
WHERE a.title LIKE 'Vue3 组合式%' AND NOT EXISTS (SELECT 1 FROM article_tag at WHERE at.article_id = a.id AND at.tag_id = t.id);
INSERT INTO `article_tag` (`article_id`, `tag_id`)
SELECT a.id, t.id FROM `article` a JOIN `tag` t ON t.name = 'Spring Boot'
WHERE a.title LIKE '从零搭建%' AND NOT EXISTS (SELECT 1 FROM article_tag at WHERE at.article_id = a.id AND at.tag_id = t.id);
INSERT INTO `article_tag` (`article_id`, `tag_id`)
SELECT a.id, t.id FROM `article` a JOIN `tag` t ON t.name = 'Docker'
WHERE a.title LIKE 'Docker Compose%' AND NOT EXISTS (SELECT 1 FROM article_tag at WHERE at.article_id = a.id AND at.tag_id = t.id);

-- ============ 随笔（朋友圈式） ============
INSERT INTO `essay` (`user_id`, `content`, `media`, `weather`, `mood`, `location`, `like_count`, `status`, `create_time`)
SELECT 1, '凌晨两点终于把这个折磨了我三天的 bug 解决了。\n\n原来是时区问题——数据库里存的是 UTC，前端直接当本地时间渲染了。这种坑不踩一次真不知道疼。\n\n纪念一下，顺便提醒自己：时间处理永远是后端最容易翻车的地方之一。', NULL, '夜', '释然', '杭州', 12, 1, '2026-07-24 02:15:00'
WHERE NOT EXISTS (SELECT 1 FROM `essay` WHERE `content` LIKE '凌晨两点终于把%');

INSERT INTO `essay` (`user_id`, `content`, `media`, `weather`, `mood`, `location`, `like_count`, `status`, `create_time`)
SELECT 1, '今天 refactoring 了一整天的老代码。\n\n删掉冗余、理清逻辑、补上注释。虽然没有新增任何功能，但看着清爽的代码，那种成就感不亚于写完一个大需求。\n\n代码也是需要经常打扫的房间。', '[{"type":"image","url":"https://picsum.photos/seed/lune-e1/600/400"}]', '晴', '满足', '上海', 8, 1, '2026-07-26 18:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `essay` WHERE `content` LIKE '今天 refactoring%');

INSERT INTO `essay` (`user_id`, `content`, `media`, `weather`, `mood`, `location`, `like_count`, `status`, `create_time`)
SELECT 1, '傍晚去河边跑步，正好撞见一场绝美的日落。\n\n天空从橘红渐变成粉紫，河面洒满了碎金。跑了五公里，出了一身汗，整个人都通透了。\n\n运动真的是性价比最高的快乐。', '[{"type":"image","url":"https://picsum.photos/seed/lune-e2/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-e3/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-e4/600/400"}]', '晴', '开心', '杭州', 21, 1, '2026-07-27 19:20:00'
WHERE NOT EXISTS (SELECT 1 FROM `essay` WHERE `content` LIKE '傍晚去河边跑步%');

INSERT INTO `essay` (`user_id`, `content`, `media`, `weather`, `mood`, `location`, `like_count`, `status`, `create_time`)
SELECT 1, '周末宅家研究新菜式——第一次做可乐鸡翅。\n\n卖相一般但味道意外地不错，配米饭能干掉两大碗。做饭的过程特别解压，看着食材在手里变成一道菜，有种踏实的幸福感。\n\n下厨房，是成年人的治愈时刻。', '[{"type":"image","url":"https://picsum.photos/seed/lune-e5/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-e6/600/400"}]', '多云', '惬意', '家', 15, 1, '2026-07-28 12:30:00'
WHERE NOT EXISTS (SELECT 1 FROM `essay` WHERE `content` LIKE '周末宅家研究新菜式%');

INSERT INTO `essay` (`user_id`, `content`, `media`, `weather`, `mood`, `location`, `like_count`, `status`, `create_time`)
SELECT 1, '读完了《山茶文具店》。\n\n一封封代笔的信，串联起小镇上温暖的人情。原来认真对待每一件小事，本身就是一种了不起的生活态度。\n\n"只要结局完美，过去的种种都算数。" 推荐给最近有点浮躁的你。', '[{"type":"image","url":"https://picsum.photos/seed/lune-e7/600/400"}]', '雨', '平静', '家', 18, 1, '2026-07-29 21:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `essay` WHERE `content` LIKE '读完了《山茶文具店》%');

-- ============ 记录（分类卡片） ============
INSERT INTO `record` (`user_id`, `category_id`, `title`, `content`, `media`, `create_time`)
SELECT 1, 5, '杭州西湖的清晨', '特意起了个大早去看没有游客的西湖。\n\n清晨的苏堤只有零星晨练的人，雾气还没散，远处的雷峰塔若隐若现。坐在长椅上发呆了很久，看松鼠在树上窜来窜去。\n\n旅行不一定要去远方，换个时间看熟悉的风景，也有不一样的惊喜。', '[{"type":"image","url":"https://picsum.photos/seed/lune-r1/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-r2/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-r3/600/400"}]', '2026-07-15 07:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `record` WHERE `title` = '杭州西湖的清晨');

INSERT INTO `record` (`user_id`, `category_id`, `title`, `content`, `media`, `create_time`)
SELECT 1, 6, '探店：巷子里的一家宝藏面馆', '朋友推荐的一家藏在老巷子里的面馆，店面不大但生意极好。\n\n招牌的腰花拌川，腰花嫩滑没有腥味，面条劲道，酱汁咸香中带一点甜。一碗下肚，浑身都暖和了。\n\n真正的美味，往往藏在最不起眼的街角。', '[{"type":"image","url":"https://picsum.photos/seed/lune-r4/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-r5/600/400"}]', '2026-07-19 12:30:00'
WHERE NOT EXISTS (SELECT 1 FROM `record` WHERE `title` = '探店：巷子里的一家宝藏面馆');

INSERT INTO `record` (`user_id`, `category_id`, `title`, `content`, `media`, `create_time`)
SELECT 1, 7, '最近循环的歌单', '整理了一下最近单曲循环的几首歌，每一首都藏着一段心情。\n\n有深夜写代码时的白噪音，有跑步时的节奏，也有午后发呆时的轻音乐。音乐是记忆的开关，某段旋律响起，就会想起某个具体的瞬间。\n\n你的单曲循环是哪一首？', '[{"type":"image","url":"https://picsum.photos/seed/lune-r6/600/400"}]', '2026-07-23 22:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `record` WHERE `title` = '最近循环的歌单');

INSERT INTO `record` (`user_id`, `category_id`, `title`, `content`, `media`, `create_time`)
SELECT 1, 4, '《塞尔达传说》通关纪念', '断断续续玩了一个月，终于通关了。\n\n在海拉鲁大陆上爬山、滑翔、做饭、打怪，每一个角落都藏着惊喜。最后打败魔王的那一刻，竟有点舍不得结束。\n\n好的游戏和好的书一样，都是一场奇妙的旅程。', '[{"type":"image","url":"https://picsum.photos/seed/lune-r7/600/400"},{"type":"image","url":"https://picsum.photos/seed/lune-r8/600/400"}]', '2026-07-26 23:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `record` WHERE `title` = '《塞尔达传说》通关纪念');

-- ============ 树洞（弹幕） ============
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '希望今年的所有努力，都能在明年开花结果。', '#ff9a9e', 23, 1, '2026-07-20 10:00:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '希望今年的所有努力%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '偷偷许个愿：愿看到这条弹幕的你，天天开心。', '#a18cd1', 45, 1, '2026-07-21 15:30:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '偷偷许个愿%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '今天也是为生活努力的一天，加油陌生人！', '#84fab0', 31, 1, '2026-07-22 09:20:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '今天也是为生活努力%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '把烦恼丢进树洞里，明天又是崭新的一天。', '#fbc2eb', 19, 1, '2026-07-23 20:45:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '把烦恼丢进树洞%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '愿所有的不期而遇，都是久别重逢。', '#8fd3f4', 52, 1, '2026-07-24 22:10:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '愿所有的不期而遇%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '深夜emo的时候，记得来这里看看，你不是一个人。', '#fccb90', 38, 1, '2026-07-25 01:30:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '深夜emo的时候%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '生活明朗，万物可爱，人间值得，未来可期。', '#e0c3fc', 67, 1, '2026-07-26 12:00:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '生活明朗%');
INSERT INTO `tree_hole` (`user_id`, `content`, `color`, `like_count`, `status`, `create_time`) SELECT 0, '希望家人身体健康，这比什么都重要。', '#a8edea', 74, 1, '2026-07-27 18:20:00' WHERE NOT EXISTS (SELECT 1 FROM `tree_hole` WHERE `content` LIKE '希望家人身体健康%');

-- ============ 评论（文章 + 随笔） ============
INSERT INTO `comment` (`article_id`, `type`, `source_id`, `user_id`, `content`, `like_count`, `status`, `create_time`)
SELECT a.id, 'article', a.id, 0, '写得太棒了！正好我也在考虑自建博客，这篇给了我很大的参考价值。', 8, 1, '2026-07-11 10:20:00' FROM `article` a WHERE a.title LIKE '从零搭建%'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.content LIKE '写得太棒了%');
INSERT INTO `comment` (`article_id`, `type`, `source_id`, `user_id`, `content`, `like_count`, `status`, `create_time`)
SELECT a.id, 'article', a.id, 0, '技术选型那部分讲得很清晰，已收藏，期待后续评论系统的实现篇！', 5, 1, '2026-07-12 14:00:00' FROM `article` a WHERE a.title LIKE '从零搭建%'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.content LIKE '技术选型那部分%');
INSERT INTO `comment` (`article_id`, `type`, `source_id`, `user_id`, `content`, `like_count`, `status`, `create_time`)
SELECT a.id, 'article', a.id, 0, 'composables 的拆解思路太实用了，正好解决了我项目里同样的痛点。', 12, 1, '2026-07-15 09:40:00' FROM `article` a WHERE a.title LIKE 'Vue3 组合式%'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.content LIKE 'composables 的拆解思路%');
INSERT INTO `comment` (`article_id`, `type`, `source_id`, `user_id`, `content`, `like_count`, `status`, `create_time`)
SELECT a.id, 'article', a.id, 0, '不记录就会遗忘，这句话戳中我了。从今天起我也要开始记录。', 15, 1, '2026-07-23 21:10:00' FROM `article` a WHERE a.title LIKE '把日子过成诗%'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.content LIKE '不记录就会遗忘%');
INSERT INTO `comment` (`article_id`, `type`, `source_id`, `user_id`, `content`, `like_count`, `status`, `create_time`)
SELECT e.id, 'essay', e.id, 0, '日出而作日落而息，你这作息够硬核的哈哈。', 3, 1, '2026-07-24 08:00:00' FROM `essay` e WHERE e.content LIKE '凌晨两点%'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.content LIKE '日出而作%');
INSERT INTO `comment` (`article_id`, `type`, `source_id`, `user_id`, `content`, `like_count`, `status`, `create_time`)
SELECT e.id, 'essay', e.id, 0, '日落拍得也太美了吧！求问这是什么位置？', 6, 1, '2026-07-27 20:00:00' FROM `essay` e WHERE e.content LIKE '傍晚去河边跑步%'
AND NOT EXISTS (SELECT 1 FROM `comment` c WHERE c.content LIKE '日落拍得也太美%');

-- ============ 家页（烂皮书日记） ============
INSERT INTO `diary` (`user_id`, `title`, `content`, `images`, `record_time`, `page_order`, `status`, `create_time`)
SELECT 1, '初见', '那天的阳光很好，你笑着向我走来，世界突然就安静了。\n\n后来才知道，心动原来是有声音的。', '["https://picsum.photos/seed/lune-d1/500/400"]', '2024-02-14', 1, 1, '2024-02-14 20:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `diary` WHERE `title` = '初见');
INSERT INTO `diary` (`user_id`, `title`, `content`, `images`, `record_time`, `page_order`, `status`, `create_time`)
SELECT 1, '第一次旅行', '一起去了海边，看日出，捡贝壳，踩在柔软的沙滩上。\n\n你负责笑，我负责拍照，那就是我理想中的幸福。', '["https://picsum.photos/seed/lune-d2/500/400","https://picsum.photos/seed/lune-d3/500/400"]', '2024-07-20', 2, 1, '2024-07-20 21:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `diary` WHERE `title` = '第一次旅行');
INSERT INTO `diary` (`user_id`, `title`, `content`, `images`, `record_time`, `page_order`, `status`, `create_time`)
SELECT 1, '一起做饭的日常', '周末的午后，一起窝在厨房研究新菜式。\n\n你切菜我掌勺，虽然手忙脚乱，但做出来的饭菜格外香。原来幸福就藏在这些烟火气里。', '["https://picsum.photos/seed/lune-d4/500/400"]', '2025-03-08', 3, 1, '2025-03-08 19:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `diary` WHERE `title` = '一起做饭的日常');
INSERT INTO `diary` (`user_id`, `title`, `content`, `images`, `record_time`, `page_order`, `status`, `create_time`)
SELECT 1, '平凡又珍贵的每一天', '不知不觉已经一起走过了这么多日子。\n\n没有轰轰烈烈，只有细水长流。想把每一个平凡的日子，都过成我们喜欢的样子。', '["https://picsum.photos/seed/lune-d5/500/400","https://picsum.photos/seed/lune-d6/500/400"]', '2026-01-01', 4, 1, '2026-01-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `diary` WHERE `title` = '平凡又珍贵的每一天');

-- ============ 家页（主信息） ============
INSERT INTO `family` (`user_id`, `title`, `content`, `cover`, `bg_cover`, `man_cover`, `woman_cover`, `man_name`, `woman_name`, `timing`, `countdown_title`, `countdown_time`, `like_count`, `status`, `create_time`)
SELECT 1, '我们的小家', '春来夏往，秋收冬藏，我们来日方长。', 'https://picsum.photos/seed/lune-fam/800/500', 'https://picsum.photos/seed/lune-fambg/1200/600', 'https://picsum.photos/seed/lune-boy/300/300', 'https://picsum.photos/seed/lune-girl/300/300', '阿辰', '小月', '2024-02-14 00:00:00', '在一起的第', '2024-02-14 00:00:00', 99, 1, '2024-02-14 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `family` WHERE `man_name` = '阿辰');

-- ============ 许愿池 ============
INSERT INTO `wish` (`user_id`, `title`, `content`, `like_count`, `status`, `create_time`) SELECT 1, '想要一个本地音乐播放器', '希望能把喜欢的歌单导入，支持歌词滚动显示和桌面歌词，界面要简洁好看，最好能自定义主题色。', 15, 1, '2026-07-20 10:30:00' WHERE NOT EXISTS (SELECT 1 FROM `wish` WHERE `title` = '想要一个本地音乐播放器');
INSERT INTO `wish` (`user_id`, `title`, `content`, `like_count`, `status`, `create_time`) SELECT 1, '做一个网页版番茄钟', '工作学习需要一个专注计时器，可以自定义工作/休息时长，完成后有提醒音效和统计报表，看看一周专注了多久。', 12, 1, '2026-07-21 14:20:00' WHERE NOT EXISTS (SELECT 1 FROM `wish` WHERE `title` = '做一个网页版番茄钟');
INSERT INTO `wish` (`user_id`, `title`, `content`, `like_count`, `status`, `create_time`) SELECT 1, '照片墙自动整理工具', '上传一堆照片后能按时间、地点自动分类，生成好看的照片墙页面，支持按年份翻页浏览。', 9, 1, '2026-07-22 09:15:00' WHERE NOT EXISTS (SELECT 1 FROM `wish` WHERE `title` = '照片墙自动整理工具');
INSERT INTO `wish` (`user_id`, `title`, `content`, `like_count`, `status`, `create_time`) SELECT 1, 'markdown 简历生成器', '用 markdown 写简历内容，一键生成漂亮的 PDF 简历，多几套模板可选，求职季太需要了。', 21, 1, '2026-07-23 16:45:00' WHERE NOT EXISTS (SELECT 1 FROM `wish` WHERE `title` = 'markdown 简历生成器');
INSERT INTO `wish` (`user_id`, `title`, `content`, `like_count`, `status`, `create_time`) SELECT 1, '网页收藏夹同步插件', '浏览器书签能同步到网页端管理，支持标签分类和全文搜索，换电脑也不怕丢书签了。', 6, 1, '2026-07-24 11:00:00' WHERE NOT EXISTS (SELECT 1 FROM `wish` WHERE `title` = '网页收藏夹同步插件');
INSERT INTO `wish` (`user_id`, `title`, `content`, `like_count`, `status`, `create_time`) SELECT 1, '每日一句英语打卡', '每天推送一句英文美句，配中文翻译和语音朗读，可以打卡记录学习天数，养成习惯。', 8, 1, '2026-07-25 08:40:00' WHERE NOT EXISTS (SELECT 1 FROM `wish` WHERE `title` = '每日一句英语打卡');

-- 许愿点赞（user_id=1 给前 3 个许愿点赞，防重复靠 uk_wish_user）
INSERT IGNORE INTO `wish_like` (`wish_id`, `user_id`) SELECT w.id, 1 FROM `wish` w ORDER BY w.id LIMIT 3;

-- ============ 站点背景图配置（picsum 占位，可后台替换） ============
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-landing/1600/900","https://picsum.photos/seed/lune-landing2/1600/900"]' WHERE `config_key` = 'landing_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-home/1600/500"]' WHERE `config_key` = 'home_hero_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-family/1600/500"]' WHERE `config_key` = 'family_hero_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-essay/1600/500"]' WHERE `config_key` = 'essay_hero_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-record/1600/500"]' WHERE `config_key` = 'record_hero_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-treehole/1600/900"]' WHERE `config_key` = 'treehole_danmaku_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-wish/1600/500"]' WHERE `config_key` = 'wish_hero_bg' AND `config_value` = '[]';
UPDATE `site_config` SET `config_value` = '["https://picsum.photos/seed/lune-resume/1600/500"]' WHERE `config_key` = 'resume_hero_bg' AND `config_value` = '[]';
