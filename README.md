# 🌙 Lune — Recording the Beauty of Life

**Lune** (French for "moon") is a meticulously designed, romantic full-stack personal blog. Named after the moon, it embodies the idea of gently capturing every beautiful moment in ordinary days.

---

## 🌐 Live Demo

<div align="center">

### ✨ [https://ztcools.com](https://ztcools.com) ✨

| 🏠 Frontend Experience | 🛠 Admin Panel |
|---|---|
| [https://ztcools.com](https://ztcools.com) | [https://ztcools.com/admin](https://ztcools.com/admin) |
| Visit directly | Account: `admin` / Password: `123123` |

**📱 We recommend trying both PC and mobile** — fully adapted for mobile (bottom TabBar navigation / touch optimization / responsive layout / PWA offline caching)

> 🔒 Full-site HTTPS (TrustAsia DV certificate) · Static asset CDN acceleration (`res.ztcools.com`) · Tencent Cloud COS object storage

</div>

---

## 📖 Table of Contents

- [Online Demo](#-live-demo)
- [Frontend Pages](#frontend-pages)
- [Admin Panel](#admin-panel)
- [Core Features](#-core-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#project-structure)
- [Deployment Guide](#-deployment-guide)
  - [Method 1: Docker Deployment (Recommended)](#method-1-docker-deployment-recommended)
  - [Method 2: Local Development (Docker Compose)](#method-2-local-development-docker-compose)
  - [Method 3: Bare-Metal Run](#method-3-bare-metal-run)
- [Ops Guide](#-ops-guide)
- [License](#-license)

---

## Frontend Pages

### Landing

![Landing](效果图/Landing页.png)

### Home

![Home](效果图/首页.png)

> ✨ CSS frosted-glass info card · Three.js WebGL shader snowflake particles falling · Card hover glass transition · Web Audio pentatonic-scale BGM player

### Essays

![Essays](效果图/随笔.png)

> ✨ WeChat Moments-style linear timeline · each post with avatar + italic body + tags · comment panel styled like Douyin's half-screen sheet, with a serif font and rounded input bar

### Records

![Records](效果图/记录.png)

> ✨ QQ Zone "shuoshuo" style feed · smart image/video grid layout (1 full-width, 2 side-by-side, 3 in an L-shape, 4 in a 2×2 grid, …) · category tag filtering

### Tree Hole

| Danmaku | Timeline |
|------|--------|
| ![Tree Hole Danmaku](效果图/树洞/首页.png) | ![Tree Hole Timeline](效果图/树洞/次页.png) |

> ✨ Top half: full-screen background + 6-lane CSS danmaku scrolling endlessly right-to-left · Bottom half: alternating left-right timeline bubbles · input syncs live to both danmaku and timeline

### Home (Family)

| Avatar & Light Beams | Masterpiece Artwork | Diary |
|------------|----------|--------|
| ![Home](效果图/家/image.png) | ![Masterpiece Artwork](效果图/家/世界名画.png) | ![Little Moments](效果图/家/点点滴滴.png) |

> ✨ Canvas sakura particle falling · SVG lightning connections + conic-gradient rotating light beams · love-heart click release animation · blessing danmaku auto-scrolling loop · **"Diary"** CSS 3D page-flip effect + Ma Shan Zheng calligraphy font, page-turn buttons simulating a real book

### Profile Card

![Profile Card](效果图/个人信息卡片.png)

> ✨ Frosted-glass card · avatar hover rotation · nickname + bio · follower/following data display

---

## Admin Panel

### Login / Register

| Login | Register |
|------|------|
| ![Login](效果图/登录/登录.png) | ![Register](效果图/登录/注册.png) |

> ✨ Gradient background · frosted-glass form · input focus animation · email verification-code registration

### System Dashboard

![Admin Dashboard](效果图/后台/系统首页.png)

### Article Management

![Article Management](效果图/后台/文章管理.png)

### Essay Management

![Essay Management](效果图/后台/随笔管理.png)

### Record Management

![Record Management](效果图/后台/记录管理.png)

### Tree Hole Management

![Tree Hole Management](效果图/后台/树洞管理.png)

### Diary Management

![Diary Management](效果图/后台/日记管理.png)

### Comment Management

![Comment Management](效果图/后台/评论管理.png)

### Category Management

![Category Management](效果图/后台/分类管理.png)

### Resource Management

![Resource Management](效果图/后台/资源管理.png)

### Site Management

![Site Management](效果图/后台/网站管理.png)

### Site Settings

| Basic Settings | Background Settings |
|----------|----------|
| ![Site Settings](效果图/后台/网站设置.png) | ![Background Settings](效果图/后台/网站设置1.png) |

> ✨ Supports configuring multiple background images per page with random switching · image preview · resource library picker · local upload

---

## ✨ Core Features

### 🎯 Product Features
- 📝 **Blog Articles** — categories/tags/pinning/views/likes/Markdown rendering
- 💭 **Essays** — Moments-style photo/video posts, with a nine-grid media layout
- 📖 **Records** — category card waterfall layout, QQ Zone style
- 🌳 **Tree Hole** — danmaku interaction + timeline dual views
- 💕 **Home Page** — couple avatar interaction, days-together counter, blessing board, diary
- 🌠 **Wishing Pool** — feature request upvote ranking + comment interaction
- 👤 **Resume Page** — profile card + work timeline + project showcase

### 📱 Full Mobile Adaptation
- 🎯 **Bottom TabBar Navigation** — frosted glass + 5 main tabs + ActionSheet "My" panel
- 🖼 **Image Lazy Loading** — `el-image` lazy + lightbox preview, long lists scroll without hogging bandwidth
- 🌄 **Portrait Backgrounds** — 14 `*_bg_mobile` configs + 9:16 portrait-cropped image sources, admin switches per device
- 📲 **PWA Offline Caching** — Service Worker precache + runtime cache (images/API/fonts)
- 🎵 **Mini Player** — mobile bottom mini music bar, tap to expand into the full player
- ✨ **Touch Feedback** — scale animations + iOS safe-area adaptation + touch haptics

### 🔒 Enterprise-Grade Security
- 🛡 **API Rate Limiting** — Nginx + backend dual-layer sliding window (auth 5r/m, writes 20/min, reads 120/min)
- 🔐 **Login Lockout** — 5 consecutive failures locks for 15 minutes (Redis + in-memory fallback)
- 🚫 **XSS Protection** — server-side escaping + frontend sanitization + CSP response headers
- 🔑 **JWT Blacklist** — stored in Redis, automatically degrades if Redis is down
- 📦 **File Whitelist** — type validation + 50MB limit + SVG rejected (anti-XSS)
- 🕵️ **Production Hardening** — sourcemaps off / terser strips console / debug shortcuts disabled

### ⚡ Performance Optimization (runs fine on a 2C4G 5M small pipe)
- 🎨 **Element Plus On-Demand Loading** — unplugin automatic tree-shaking (1MB → 92KB)
- ❄️ **Three.js On-Demand Loading** — `defineAsyncComponent`, loaded only on PC
- 📦 **Fully Self-Hosted Media** — backgrounds/music ship inside the nginx image, no third-party random images or external links
- 🗜 **JVM 512M + MySQL 768M + Redis 256M** — precise resource limits
- 📝 **Log Rotation** — container logs auto-cleaned at 10MB × 3-5 files
- 🖼 **WebP Image Sources** — backgrounds unified to WebP, mobile also serves a portrait crop (~30% of the landscape size)

### 🤖 AI Agent (Luna)
- 💬 **Natural Language Management** — WeChat-style chat, publish articles / query data / change config with a single sentence
- 🛠 **22 Tool Functions** — covering full CRUD for articles/essays/records/tree hole/wishing pool/site config/resume
- 👁 **Preview Before Publishing** — Agent creates a draft → preview with the same home-page card → one-click publish
- 🧠 **Short-Term Memory** — Redis stores the day's conversation, auto-cleared daily at 00:00
- ⚡ **SSE Streaming** — typewriter-effect live output, no page reload
- 🔌 **Standalone Service** — lune-agent (Spring Boot :8082), high cohesion and low coupling

### 🏗 Architecture Design
- 🔄 **Storage Abstraction** — `StorageService` interface, one-click switch between local / OSS
- 🗄 **Soft Delete** — User/Article/Essay/Record/WorkExperience/Project/Wish
- ⚛️ **Atomic Counters** — view/like counts use atomic SQL updates to avoid concurrent loss
- 🔁 **Idempotent Initialization** — `DataInitializer` only inserts into empty tables, safe to re-run
- 📊 **Unified Responses** — `Result<T>` + `PageResult` + global exception handling

---

## 🛠 Tech Stack

| Layer | Technology |
|----|------|
| **Backend** | Java 17 · Spring Boot 3.3 · MyBatis-Plus 3.5 · Spring Security + JWT |
| **Frontend** | Vue 3 (Composition API) · Vite 5 · Pinia · Vue Router 4 · Element Plus |
| **Effects** | Three.js GLSL shaders (snow) · Canvas particles (sakura) · CSS 3D page flip · CSS danmaku |
| **Database** | MySQL 8 · Redis |
| **PWA** | vite-plugin-pwa · Workbox · Service Worker |
| **Ops** | Docker · Docker Compose · Nginx · domestic mirror acceleration |
| **AI Agent** | DeepSeek v4 Flash · Function Calling · SSE Streaming · Redis memory |

---

## Project Structure

```
Lune/
├── lune-server/                  # Backend Spring Boot project (Maven multi-module)
│   ├── pom.xml                   # Parent POM
│   └── lune-web/                 # Web module
│       ├── src/main/java/com/lune/
│       │   ├── config/           # Security, CORS, MyBatis-Plus config
│       │   ├── security/         # JWT authentication filters
│       │   ├── entity/           # Database entities
│       │   ├── dto/              # Request/Response DTOs
│       │   ├── mapper/           # MyBatis-Plus Mappers
│       │   ├── service/          # Business logic
│       │   └── controller/       # REST controllers
│       └── src/main/resources/
│           ├── application.yml           # Main config (env-var parameterized)
│           ├── application-dev.yml       # Development profile
│           ├── application-prod.yml      # Production profile
│           └── application-local.yml.template  # Local config template
├── lune-ui/                      # Frontend Vue 3 project
│   ├── src/
│   │   ├── api/                  # Axios wrapper + API functions
│   │   ├── router/               # Vue Router routes
│   │   ├── stores/               # Pinia state management
│   │   ├── layout/               # Layout components
│   │   ├── views/                # Page components
│   │   └── assets/               # Styles, fonts
│   └── public/assets/            # Static assets
├── lune-agent/                   # Standalone AI Agent service
│   ├── src/main/java/com/lune/agent/
│   │   ├── pipeline/             # Core pipeline (Orchestrator → LLM → Tools)
│   │   ├── memory/               # Redis short-term memory (daily TTL)
│   │   ├── client/               # LuneApiClient (calls lune-web REST)
│   │   ├── llm/                  # DeepSeek API client
│   │   └── controller/           # SSE streaming endpoint
│   ├── Dockerfile
│   └── README.md                 # Agent architecture docs
├── docker/                       # Docker config
│   ├── backend/Dockerfile        # Backend multi-stage build
│   ├── frontend/Dockerfile.dev   # Frontend dev image
│   ├── nginx/                    # Nginx config + production Dockerfile
│   ├── mysql/                    # MySQL config + init SQL
│   ├── redis/                    # Redis config
│   └── fonts/                    # Self-hosted fonts
├── docker-compose.dev.yml        # Dev environment orchestration
├── docker-compose.prod.yml       # Production environment orchestration
├── .env.template                 # Production env-var template
├── .env.local.template           # Local dev env-var template
├── deploy.sh                     # One-click deployment script
├── backup.sh                     # Database backup script
└── Makefile                      # Convenience commands
```

---

## 🚀 Deployment Guide

> 📘 **Full enterprise deployment docs (including connecting a domain after ICP filing / HTTPS / CDN / OSS, resource budgeting, ops troubleshooting) at [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)**
>
> 🌐 **Current production deployment report (including server architecture diagram / resource limits / Phase 2-5 incremental roadmap) at [docs/SERVER-DEPLOYMENT.md](docs/SERVER-DEPLOYMENT.md)**
>
> The demo site is deployed on a Tencent Cloud 2C4G 5M server (https://ztcools.com), a reference for best practices in resource-constrained environments.

### Method 1: Docker Deployment (Recommended)

For production on a server, one-click deployment of all services (Nginx + Spring Boot + MySQL + Redis).

#### Prerequisites

- Server OS: Ubuntu / Debian / CentOS
- Recommended spec: 2 cores / 4GB or more (this setup is tuned for 2C4G + 5M bandwidth)
- Ports to open: `80` (HTTP), `443` (HTTPS after ICP filing)

#### Security Features (Built-In)

- 🔒 API rate limiting (Nginx + backend dual layer, anti-crawler / anti-brute-force)
- 🔐 Login failure lockout (5 consecutive failures locks for 15 minutes)
- 🛡️ Full security response headers + CSP + stored-XSS filtering
- 🚫 Production disables sourcemaps + debug shortcuts (reasonable hardening)
- 📦 File storage abstraction, one-click switch between local / OSS (CDN interface reserved)

#### Deployment Steps

```bash
# 1. SSH into the server
ssh user@your-server-ip

# 2. Install git (if not installed)
sudo apt-get install -y git

# 3. Clone the project
git clone https://github.com/ztcools/Lune.git /opt/lune
cd /opt/lune

# 4. One-click deployment
bash deploy.sh --prod
```

The deployment script automatically:
- Detects and installs Docker + Docker Compose
- Generates the `.env` env-var file automatically (including random secure passwords)
- Builds the Docker images
- Starts all services and waits for health checks to pass
- Outputs the access URL and default admin account

```bash
# Check service status
make status

# View logs
make prod-logs

# Restart services
make restart
```

#### First Login

- URL: `https://your-domain`
- Admin: `https://your-domain/admin/login`
- Default admin: `admin` / `admin123`
- ⚠️ **Change the password immediately after first login!**
- 🔒 **HTTPS certificates** are supported out of the box (place the certificate in the `ssl/` directory and mount it via docker-compose)

#### Configure SSL / HTTPS (After ICP Filing)

After filing, install Certbot and configure SSL:

```bash
sudo apt-get install -y certbot
sudo certbot certonly --standalone -d your-domain.com
# Edit docker/nginx/nginx.prod.conf to add the SSL certificate paths
make restart
```

---

### Method 2: Local Development (Docker Compose)

For local development and debugging, with frontend HMR hot reload.

#### Prerequisites

- Docker + Docker Compose
- Git

#### Startup Steps

```bash
# 1. Clone the project
git clone https://github.com/ztcools/Lune.git
cd Lune

# 2. Initialize the local environment
make setup-dev
# Follow the prompts to edit .env.local with your local dev credentials

# 3. Download fonts (first run, optional)
bash docker/fonts/download-fonts.sh

# 4. Start the dev environment
make dev
```

#### Access URLs

| Service | Address | Description |
|------|------|------|
| Frontend | `http://localhost:8080` | Accessed via Nginx proxy |
| Vite HMR | `http://localhost:5173` | Vite dev server (hot reload) |
| Backend API | `http://localhost:8081/api` | Spring Boot REST API |
| MySQL | `localhost:3306` | Database |
| Redis | `localhost:6379` | Cache |

#### Hot Reload Notes

- **Frontend**: modify any file under `lune-ui/src/`, Vite HMR auto-refreshes the browser
- **Backend**: after changing Java code, run `make restart-backend` to rebuild

#### Common Commands

```bash
make dev           # Start the dev environment
make dev-logs      # View logs
make down          # Stop all containers
make clean         # Stop and remove data volumes
make status        # View container status
make shell-backend # Enter backend shell
make shell-db      # Enter MySQL
```

---

### Method 3: Bare-Metal Run

For environments without Docker.

#### Environment Requirements

Java 17 · Maven 3.8+ · Node.js 18+ · MySQL 8.0 · Redis

#### Startup Steps

```bash
# 1. Initialize the database
mysql -u root -p < lune-server/lune-web/src/main/resources/sql/lune.sql

# 2. Configure environment variables
# Copy and edit the config file
cp lune-server/lune-web/src/main/resources/application-local.yml.template \
   lune-server/lune-web/src/main/resources/application-local.yml

# 3. Set environment variables (or edit application-local.yml)
export DB_USERNAME=root
export DB_PASSWORD=your-password
export JWT_SECRET=your-jwt-secret

# 4. Start the backend
cd lune-server/lune-web
mvn spring-boot:run -Dspring-boot.run.profiles=local
# → http://localhost:8081

# 5. Start the frontend (new terminal)
cd lune-ui
npm install
npm run dev
# → http://localhost:5173
```

---

## 📋 Ops Guide

### Database Backup

```bash
# Manual backup
make backup
# Backup file: ./backups/lune_YYYYMMDD_HHMMSS.sql.gz

# Set up automatic backups (daily at 3:00 AM)
bash backup.sh --cron

# Restore a backup
make restore FILE=backups/lune_20260101_030000.sql.gz
```

### Viewing Logs

```bash
make prod-logs       # All production logs
make backend-logs    # Backend only
make nginx-logs      # Nginx only
```

### Updating a Deployment

```bash
cd /opt/lune
git pull
make prod         # Rebuild and start
```

### Environment Variables

| Variable | Description | Required |
|------|------|------|
| `DB_ROOT_PASSWORD` | MySQL root password | ✅ |
| `DB_USERNAME` | Database username | ✅ |
| `DB_PASSWORD` | Database password | ✅ |
| `JWT_SECRET` | JWT signing key (64-char random string) | ✅ |
| `REDIS_PASSWORD` | Redis password | ❌ |
| `MAIL_HOST` | SMTP server address | ❌ |
| `MAIL_USERNAME` | Email account | ❌ |
| `MAIL_PASSWORD` | Email authorization code | ❌ |
| `NGINX_PORT` | Nginx external port | ❌ (default 80) |
| `ADMIN_DEFAULT_PASSWORD` | Default admin initial password | ❌ (default admin123) |
| `AGENT_API_KEY` | AI Agent API key | ❌ (Agent unavailable if unset) |
| `AGENT_BASE_URL` | LLM API gateway URL | ❌ (default aigw.phigent.cn) |
| `AGENT_MODEL` | Model name | ❌ (default deepseek/deepseek-v4-flash) |

> 📘 Detailed Agent architecture docs at [lune-agent/README.md](lune-agent/README.md)

---

## 📄 License

MIT
