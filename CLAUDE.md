# Lune - 记录美好生活

## Project Overview

Lune is a full-stack personal blog and lifestyle web application. Fully containerized with Docker Compose for both dev and production.

- **Backend**: Spring Boot 3.3.5 + Java 17 + Maven
- **Frontend**: Vue 3 + Vite 5 + Pinia + Vue Router 4 + Element Plus
- **Database**: MySQL 8.0 + Redis 7 (Docker containers)
- **Persistence**: MyBatis-Plus 3.5.7 (logic delete on User/Article/Essay/Record/WorkExperience/Project/Wish)
- **Security**: Spring Security + JWT (jjwt 0.12.6) + BCrypt
- **Fonts**: 拉丁字体自托管 (`public/assets/fonts/*.woff2` — Fredoka, Comfortaa, Quicksand, Caveat)；中文书法体 (Ma Shan Zheng, Zhi Mang Xing, Long Cang) 首屏渲染后经国内镜像 `fonts.loli.net` 延迟注入 (`utils/loadFonts.js`)。**不直连 Google Fonts**（大陆不可达 + 渲染阻塞）
- **DevOps**: Docker Compose, Nginx reverse proxy, multi-stage Dockerfiles

**Feature Modules**:
- 博客首页（文章/分类/音乐播放器）
- 家页（恋爱记录：情侣头像互动、在一起天数、祝福板、烂皮书日记）
- 树洞（弹幕互动 + 时间线）
- 随笔（朋友圈式图文/视频动态）
- 记录（分类卡片瀑布流）
- 简历页（个人卡片 + 工作时间线 + 项目卡片，Landing 进入）
- 许愿池（需求点赞排行 + 评论）

---

## Project Structure

```
lune-server/                   # Maven parent POM
├── pom.xml
└── lune-web/                  # Spring Boot web module
    ├── pom.xml
    └── src/main/java/com/lune/
        ├── LuneApplication.java
        ├── common/            # Result, PageResult, BusinessException, GlobalExceptionHandler
        ├── config/            # Security, CORS, MyBatis-Plus, Redis, WebMvc, DataInitializer
        ├── security/          # JwtTokenProvider, JwtAuthFilter, SecurityUtils
        ├── entity/            # User, Article, Category, Tag, Comment, Essay, Record, TreeHole, Family, Diary, SiteConfig, Resource, VisitLog, WorkExperience, Project, Wish, WishLike
        ├── dto/               # Request/Response DTOs
        ├── mapper/            # MyBatis-Plus BaseMapper interfaces
        ├── service/           # Service interfaces + impl/
        └── controller/        # Public + admin REST controllers

lune-ui/                       # Vue 3 frontend
├── index.html                 # 自托管字体 + loli.net 镜像 preconnect（无 Google Fonts）
├── vite.config.js             # Port 5173, proxy /api & /upload → localhost:8081
├── package.json               # vue 3.4, pinia 2, element-plus 2, three, axios
└── src/
    ├── api/                   # request.js (axios) + modules.js (all API functions)
    ├── router/                # Landing → PublicLayout(children) + AdminLogin + AdminLayout
    ├── stores/                # user.js (auth), app.js (config + bgImages)
    ├── composables/           # usePageBackground, useAuth
    ├── layout/                # PublicLayout, AdminLayout
    ├── views/                 # landing, home, article, family, treehole, essay, record
    ├── admin/                 # Login, Dashboard, Settings, CRUD management pages
    ├── components/            # ProfileCard, MiniProfileCard, LoginCard, ArticleReader, SakuraFall, PixelSnow
    └── assets/styles/         # variables.css, global.css, animations.css

docker/                        # Docker configuration
├── backend/Dockerfile         # Multi-stage: Maven build + JRE Alpine
├── frontend/Dockerfile.dev    # Vite dev server with HMR
├── nginx/                     # Dockerfile (prod) + nginx.dev.conf + nginx.prod.conf
├── mysql/                     # my.cnf + init/01-init.sql
├── redis/                     # redis.conf (AOF persistence)
└── fonts/                     # download-fonts.sh + fonts.css

docker-compose.dev.yml         # Development: nginx + backend + frontend(Vite HMR) + mysql + redis
docker-compose.prod.yml        # Production: nginx(含前端) + backend + mysql + redis
.env.template                  # 生产环境变量模板（提交Git）
.env.local.template            # 本地开发环境变量模板（提交Git）
docker-compose.server.yml      # 生产服务器上 /opt/lune/docker-compose.yml 的来源（无 build:，只用 docker load 的镜像）
deploy.sh                      # 服务器一键部署脚本（需源码目录，故不适用于本项目的生产机）
backup.sh                      # 数据库自动备份
prune-orphan-resources.sh      # 素材库死指针体检/清理（依赖机器上文件是否存在，故不放进 migration）
Makefile                       # 便捷命令集合
```

---

## Architecture Patterns

### Backend

- **Layered**: Controller → Service → Mapper → Entity
- **Response**: `Result<T>` = `{ code, message, data }`
- **Pagination**: `PageResult.of(records, total, page, size)`
- **Constructor injection** (not `@Autowired`), `var` for locals (Java 17)
- **Dual controllers**: Public (`/api/{resource}`) + Admin (`/api/admin/{resource}`, `@PreAuthorize`)
- **LambdaQueryWrapper** for all queries, no XML mappers
- **Soft delete**: `@TableLogic` on User, Article, Essay, Record; hard delete on others
- **Security**: Stateless JWT, CSRF disabled, BCrypt passwords
- **RateLimitFilter**: Nginx + 后端双层限流（auth 5r/m，写 20/min，读 120/min），Redis 滑动窗口 + 内存降级
- **LoginAttemptService**: 登录连续失败 5 次锁定 15 分钟（Redis + 内存降级）
- **SecurityHeadersFilter**: CSP / X-Frame-Options / Permissions-Policy 等，HSTS 由 `HSTS_ENABLED` 控制
- **XssSanitizer**: 评论/树洞等纯文本字段服务端转义防存储型 XSS
- **Health**: Spring Actuator at `/api/actuator/health`
- **GlobalExceptionHandler**: Returns HTTP 500 for unhandled exceptions (not 200)
- **JwtAuthFilter**: Redis token blacklist with graceful fallback if Redis is down
- **StorageService 抽象**: `LocalStorageService`(默认) / `OssStorageService`(预留, `STORAGE_TYPE=oss` 切换)，CDN 由 `OSS_CDN_DOMAIN` 配置
- **ResourceServiceImpl**: File type whitelist validation, 50MB size limit, SVG 拒绝(XSS)
- **ArticleServiceImpl**: Atomic SQL for view/like counts (avoids race conditions)
- **AuthServiceImpl**: `@Transactional` on register, `SecureRandom` for codes

### Frontend

- **Axios**: `baseURL: '/api'`, Bearer token interceptor, unwraps `data.data` on success, 401 → clear token + redirect admin login
- **Element Plus**: 按需加载（unplugin-vue-components），图标仍全量注册
- **性能**: 路由懒加载；PixelSnow(three.js ~500KB) 经 `defineAsyncComponent` 仅 PC 按需加载；生产去 console/sourcemap
- **安全加固** (`utils/security.js`, 仅生产): 禁右键/调试快捷键、DevTools 检测、生产关 sourcemap
- **State**: Pinia stores — `user.js` (auth), `app.js` (config, bgImages, dark mode)
- **Routing**: `PublicLayout` wraps public pages, `AdminLayout` wraps admin (auth guard), scroll restoration on back/forward, catch-all 404 → `/`
- **Backgrounds**: `usePageBackground(key)` composable — ref-based with random pick from JSON array, reactive to config changes
- **Content backgrounds**: `PageBg.vue` — QQ空间式极淡固定背景图 + 动态渐变光斑 (green/pink/blue variants)
- **Effects** (`components/effects/`): `FloatPetals` (花瓣/落叶/雪花，移动端减半), `Spotlight` (聚光灯光斑), `WalkingDog`; `SakuraFall` (canvas 樱花), `PixelSnow` (three.js WebGL 雪, PC only)
- **MusicPlayer.vue**: HTML5 Audio，读 `home_music_list` 配置，含唱片旋转/进度/歌词/上下首
- **MediaEditor.vue** (admin): 可复用图片/视频九宫格编辑器（随笔/记录/简历共用）
- **Settings.vue**: 基础信息 + 首页音乐歌单 + 页面背景管理（上传即生效，无二次确认），`configLoaded` 守卫防止回填误报保存
- No `Math.random()` in computed properties (all use refs with explicit triggers)
- **UI scale**: `html { font-size: 15px }` 中等偏小，移动端特效自动降级
- **Fonts**: 书法体 (`--calligraphy-font` Ma Shan Zheng), 手写体 (`--handwriting-font` Long Cang), 正文 (`--article-font` Noto Serif SC), 个性体 (`--trendy-font` Fredoka)

---

## Environment Configuration

### .env.local (本地开发，gitignored)
```bash
DB_ROOT_PASSWORD=xxx    DB_USERNAME=root    DB_PASSWORD=xxx
REDIS_PASSWORD=         JWT_SECRET=xxx
MAIL_HOST=smtp.qq.com   MAIL_USERNAME=xxx   MAIL_PASSWORD=xxx
NGINX_PORT=8080         MYSQL_PORT=3306      REDIS_PORT=6379
```

### application.yml (parameterized, no hardcoded secrets)
```yaml
spring.datasource.url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/lune
spring.datasource.password: ${DB_PASSWORD:}    # no default fallback
app.jwt.secret: ${JWT_SECRET:}                 # no default fallback
app.upload.path: ${UPLOAD_PATH:./upload}
app.admin.default-password: ${ADMIN_DEFAULT_PASSWORD:admin123}
```

### Profiles
- `application-dev.yml`: Docker hostnames (mysql/redis), DEBUG logging
- `application-prod.yml`: Connection pooling, graceful shutdown, WARN logging
- `application-local.yml` (gitignored): mail overrides via env vars

---

## Background Image System

Each page section has a dedicated `site_config` key storing a JSON array of image URLs:

| Config Key | Page Section |
|------------|-------------|
| `landing_bg` | Landing 页 |
| `home_hero_bg` | 首页顶部 Banner |
| `home_content_bg` | 首页内容区 |
| `family_hero_bg` | 家页顶部 Banner |
| `family_content_bg` | 家页内容区 |
| `treehole_danmaku_bg` | 树洞弹幕区 |
| `treehole_content_bg` | 树洞时间线 |
| `essay_hero_bg` | 随笔顶部 Banner |
| `essay_content_bg` | 随笔内容区 |
| `record_hero_bg` | 记录顶部 Banner |
| `record_content_bg` | 记录内容区 |
| `wish_hero_bg` | 许愿池顶部 Banner |
| `wish_content_bg` | 许愿池内容区 |
| `resume_hero_bg` | 简历页顶部 Banner |

Value format: `["/upload/xxx.jpg", "/upload/yyy.png"]`

**Other site_config keys**: `beian_icp` (Landing 备案号), `home_music_list` (音乐歌单 JSON `[{name,artist,url,cover,lrc}]`), `resume_skills` / `resume_hobbies` / `resume_github` / `resume_motto` / `resume_tags` (简历个人卡片).

**Content backgrounds**渲染为 `PageBg` 透明背景（不干扰内容），仅 hero banner 用背景图主视觉。

Each page uses `const bg = usePageBackground('key')` → returns `ref<string>` with random image picked from array on config change.

---

## How to Run

### Docker (推荐)
```bash
# 本地开发
cp .env.local.template .env.local   # 编辑填入凭据
make dev                             # http://localhost:8080

# 服务器生产
bash deploy.sh --prod                # 一键部署
```

### 裸机（不推荐）
```bash
cd lune-server/lune-web && mvn spring-boot:run -Dspring-boot.run.profiles=local
cd lune-ui && npm install && npm run dev
```

Default admin: `admin` / `admin123` (可配置 `ADMIN_DEFAULT_PASSWORD`)

---

## Docker Services

| Service | Dev Port | Prod Port | Image |
|---------|----------|-----------|-------|
| nginx | 8080 | 80 | nginx:1.27-alpine (prod: 含前端构建) |
| backend | 8081 | 127.0.0.1:8081 | eclipse-temurin:17-jre-alpine |
| mysql | 3306 | 127.0.0.1:3306 | mysql:8.0 |
| redis | 6379 | 127.0.0.1:6379 | redis:7-alpine |
| frontend (dev) | 5173 | — | node:20-alpine (Vite HMR) |

---

## Database

- SQL schema: `lune-server/lune-web/src/main/resources/sql/lune.sql`
- Docker init: `docker/mysql/init/01-init.sql`
- `DataInitializer.java` auto-creates admin user + categories + site configs + resume/wish seed data (幂等，空表才插)
- Incremental migrations (`sql/migration-*.sql`，全部幂等，deploy.sh 自动执行)：
  - `migration-20260728.sql` 新增 work_experience/project/wish/wish_like 表 + essay.media
  - `migration-20260730.sql` visit_log 地区字段 + 移动端背景 key
  - `migration-20260730b-media.sql` record/work_experience/project 媒体字段
  - `migration-20260730c-localize-demo-media.sql` 演示数据里的 unsplash 外链 → 自托管 `/media/bg/*.webp`
- `user.email` has `UNIQUE` constraint
- New tables: `work_experience` (工作时间线), `project` (项目), `wish` + `wish_like` (许愿点赞), `essay.media` (朋友圈媒体)
- Backup: `make backup` or `bash backup.sh --cron` (daily at 3am)

---

## Key Conventions

- No hardcoded credentials in source code — all via env vars
- Sensitive files (`.env`, `.env.local`, `application-local.yml`) are gitignored
- Templates (`.env.template`, `.env.local.template`, `application-local.yml.template`) are committed
- `Math.random()` never inside `computed()` — always in `ref` + explicit trigger
- All `setInterval`/event listeners cleaned up in `onUnmounted`
- Atomic SQL updates for counters (avoid race conditions)
- File uploads validated (extension whitelist + size limit)
