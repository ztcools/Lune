# Lune - 记录美好生活

## Project Overview

Lune is a full-stack personal blog and lifestyle web application. Fully containerized with Docker Compose for both dev and production.

- **Backend**: Spring Boot 3.3.5 + Java 17 + Maven
- **Frontend**: Vue 3 + Vite 5 + Pinia + Vue Router 4 + Element Plus
- **Database**: MySQL 8.0 + Redis 7 (Docker containers)
- **Persistence**: MyBatis-Plus 3.5.7 (logic delete on User/Article/Essay/Record)
- **Security**: Spring Security + JWT (jjwt 0.12.6) + BCrypt
- **Fonts**: Google Fonts CDN (Noto Sans SC, ZCOOL XiaoWei, Fredoka, Comfortaa, Quicksand)
- **DevOps**: Docker Compose, Nginx reverse proxy, multi-stage Dockerfiles

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
        ├── entity/            # User, Article, Category, Tag, Comment, Essay, Record, TreeHole, Family, Diary, SiteConfig, Resource, VisitLog
        ├── dto/               # Request/Response DTOs
        ├── mapper/            # MyBatis-Plus BaseMapper interfaces
        ├── service/           # Service interfaces + impl/
        └── controller/        # Public + admin REST controllers

lune-ui/                       # Vue 3 frontend
├── index.html                 # Google Fonts CDN
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
deploy.sh                      # 服务器一键部署脚本
backup.sh                      # 数据库自动备份
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
- **Health**: Spring Actuator at `/api/actuator/health`
- **GlobalExceptionHandler**: Returns HTTP 500 for unhandled exceptions (not 200)
- **JwtAuthFilter**: Redis token blacklist with graceful fallback if Redis is down
- **ResourceServiceImpl**: File type whitelist validation, 50MB size limit
- **ArticleServiceImpl**: Atomic SQL for view/like counts (avoids race conditions)
- **AuthServiceImpl**: `@Transactional` on register, `SecureRandom` for codes

### Frontend

- **Axios**: `baseURL: '/api'`, Bearer token interceptor, unwraps `data.data` on success
- **State**: Pinia stores — `user.js` (auth), `app.js` (config, bgImages, dark mode)
- **Routing**: `PublicLayout` wraps public pages, `AdminLayout` wraps admin (auth guard)
- **Backgrounds**: `usePageBackground(key)` composable — ref-based with random pick from JSON array, reactive to config changes
- **Settings.vue**: Card-based layout, per-page multi-image background management with preview
- No `Math.random()` in computed properties (all use refs with explicit triggers)

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

Value format: `["/upload/xxx.jpg", "/upload/yyy.png"]`

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
- `DataInitializer.java` auto-creates admin user + categories + site configs on empty DB
- `user.email` has `UNIQUE` constraint
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
