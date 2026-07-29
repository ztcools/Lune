# 🌙 Lune — 记录美好生活

**Lune**（法语：月亮）是一个精心设计、充满浪漫气息的全栈个人博客。以月为名，寓意在平凡的日子里温柔地记录下每一刻美好。

---

## 🌐 在线演示

<div align="center">

### ✨ [http://111.231.14.63](http://111.231.14.63) ✨

| 🏠 前台体验 | 🛠 后台管理 |
|---|---|
| [http://111.231.14.63](http://111.231.14.63) | [http://111.231.14.63/admin](http://111.231.14.63/admin) |
| 直接访问即可 | 账号：`admin` ／ 密码：`admin123` |

**📱 建议同时用 PC 和手机访问体验** — 已做移动端全面适配（底部 TabBar 导航 / 触摸优化 / 响应式布局 / PWA 离线缓存）

</div>

---

## 📖 目录

- [在线演示](#-在线演示)
- [前台页面](#前台页面)
- [后台管理](#后台管理)
- [核心特性](#-核心特性)
- [技术栈](#-技术栈)
- [项目结构](#项目结构)
- [部署指南](#-部署指南)
  - [方式一：Docker 部署（推荐）](#方式一docker-部署推荐)
  - [方式二：本地开发（Docker Compose）](#方式二本地开发docker-compose)
  - [方式三：裸机运行](#方式三裸机运行)
- [运维指南](#-运维指南)
- [License](#-license)

---

## 前台页面

### Landing

![Landing](效果图/Landing页.png)

### 首页

![首页](效果图/首页.png)

> ✨ CSS 毛玻璃信息卡 · Three.js WebGL 着色器雪花粒子飘落 · 卡片悬浮玻璃态过渡 · Web Audio 五声音阶 BGM 播放器

### 随笔

![随笔](效果图/随笔.png)

> ✨ 微信朋友圈风格线性时间流 · 每条头像 + 斜体正文 + 标签 · 评论区仿抖音半屏面板，楷体字体 + 圆角输入条

### 记录

![记录](效果图/记录.png)

> ✨ QQ 空间说说风格 Feed · 图片/视频智能网格布局（1 张全宽、2 张并排、3 张 L 形、4 张 2×2……）· 分类标签筛选

### 树洞

| 弹幕 | 时间线 |
|------|--------|
| ![树洞弹幕](效果图/树洞/首页.png) | ![树洞时间线](效果图/树洞/次页.png) |

> ✨ 上半屏全屏背景 + 6 车道 CSS 弹幕从右向左无限飘过 · 下半屏左右交替时间线气泡 · 输入内容实时同步弹幕 + 时间线

### 家

| 头像 & 光束 | 世界名画 | 烂皮书 |
|------------|----------|--------|
| ![家](效果图/家/image.png) | ![世界名画](效果图/家/世界名画.png) | ![点点滴滴](效果图/家/点点滴滴.png) |

> ✨ Canvas 樱花粒子飘落 · SVG 闪电连线 + conic-gradient 旋转光束 · 爱心点击放飞动画 · 祝福弹幕自动循环滚动 · **「烂皮书」** CSS 3D 翻页效果 + 马善政草书字体，翻页按钮模拟真实翻书

### 个人信息卡片

![个人信息卡片](效果图/个人信息卡片.png)

> ✨ 毛玻璃卡片 · 头像 hover 旋转 · 昵称 + 简介 · 关注/粉丝数据展示

---

## 后台管理

### 登录 / 注册

| 登录 | 注册 |
|------|------|
| ![登录](效果图/登录/登录.png) | ![注册](效果图/登录/注册.png) |

> ✨ 渐变背景 · 毛玻璃表单 · 输入框聚焦动画 · 邮箱验证码注册

### 系统首页

![后台首页](效果图/后台/系统首页.png)

### 文章管理

![文章管理](效果图/后台/文章管理.png)

### 随笔管理

![随笔管理](效果图/后台/随笔管理.png)

### 记录管理

![记录管理](效果图/后台/记录管理.png)

### 树洞管理

![树洞管理](效果图/后台/树洞管理.png)

### 日记管理

![日记管理](效果图/后台/日记管理.png)

### 评论管理

![评论管理](效果图/后台/评论管理.png)

### 分类管理

![分类管理](效果图/后台/分类管理.png)

### 资源管理

![资源管理](效果图/后台/资源管理.png)

### 网站管理

![网站管理](效果图/后台/网站管理.png)

### 网站设置

| 基础设置 | 背景设置 |
|----------|----------|
| ![网站设置](效果图/后台/网站设置.png) | ![背景设置](效果图/后台/网站设置1.png) |

> ✨ 支持分页面配置多张背景图随机切换 · 图片预览 · 资源库选择 · 本地上传

---

## ✨ 核心特性

### 🎯 产品功能
- 📝 **博客文章** — 分类/标签/置顶/浏览量/点赞/Markdown 渲染
- 💭 **随笔** — 朋友圈式图文/视频动态，支持九宫格媒体
- 📖 **记录** — 分类卡片瀑布流，QQ空间风格
- 🌳 **树洞** — 弹幕互动 + 时间线双视图
- 💕 **家页** — 情侣头像互动、在一起天数、祝福板、烂皮书日记
- 🌠 **许愿池** — 需求点赞排行 + 评论互动
- 👤 **简历页** — 个人卡片 + 工作时间线 + 项目展示

### 📱 移动端全面适配
- 🎯 **底部 TabBar 导航** — 毛玻璃 + 5 主 Tab + ActionSheet 我的面板
- 🖼 **智能图片** — 懒加载 + 模糊占位 + 响应式 srcset + 弱网降级
- 🌐 **网络感知** — `navigator.connection` API 检测 2G/3G/4G 自动调清晰度
- 📲 **PWA 离线缓存** — Service Worker 预缓存 + 运行时缓存（图片/API/字体）
- 🎵 **迷你播放器** — 移动端底部迷你音乐条，点击展开完整播放
- ✨ **触摸反馈** — 缩放动画 + iOS 安全区适配 + 触摸震动

### 🔒 企业级安全
- 🛡 **接口限流** — Nginx + 后端双层滑动窗口（auth 5r/m、写 20/min、读 120/min）
- 🔐 **登录锁定** — 连续失败 5 次锁定 15 分钟（Redis + 内存降级）
- 🚫 **XSS 防护** — 服务端转义 + 前端净化 + CSP 响应头
- 🔑 **JWT 黑名单** — Redis 存储，Redis 宕机自动降级
- 📦 **文件白名单** — 类型校验 + 50MB 限制 + 拒绝 SVG（防 XSS）
- 🕵️ **生产加固** — 关 sourcemap / terser 去 console / 禁调试快捷键

### ⚡ 性能优化（2C4G 5M 小水管也能跑）
- 🎨 **Element Plus 按需加载** — unplugin 自动拆包（1MB → 92KB）
- ❄️ **Three.js 按需加载** — `defineAsyncComponent` 仅 PC 加载
- 🌍 **国内 CDN 资源** — alcy.cc 二次元图床 / jsdelivr 音乐
- 🗜 **JVM 512M + MySQL 768M + Redis 256M** — 精准资源限制
- 📝 **日志轮转** — 容器日志 10MB × 3-5 份自动清理
- 🎯 **响应式图片** — 400w / 800w / 1600w 按设备自动选

### 🏗 架构设计
- 🔄 **存储抽象** — `StorageService` 接口，本地 / OSS 一键切换
- 🗄 **软删除** — User/Article/Essay/Record/WorkExperience/Project/Wish
- ⚛️ **原子计数** — 浏览/点赞用 SQL 原子更新，避免并发丢失
- 🔁 **幂等初始化** — `DataInitializer` 空表才插，安全重复执行
- 📊 **统一响应** — `Result<T>` + `PageResult` + 全局异常处理

---

## 🛠 技术栈

| 层 | 技术 |
|----|------|
| **后端** | Java 17 · Spring Boot 3.3 · MyBatis-Plus 3.5 · Spring Security + JWT |
| **前端** | Vue 3 (Composition API) · Vite 5 · Pinia · Vue Router 4 · Element Plus |
| **特效** | Three.js GLSL 着色器（雪花）· Canvas 粒子（樱花）· CSS 3D 翻页 · CSS 弹幕 |
| **数据库** | MySQL 8 · Redis |
| **PWA** | vite-plugin-pwa · Workbox · Service Worker |
| **运维** | Docker · Docker Compose · Nginx · 国内镜像加速 |

---

## 项目结构

```
Lune/
├── lune-server/                  # 后端 Spring Boot 项目（Maven 多模块）
│   ├── pom.xml                   # 父 POM
│   └── lune-web/                 # Web 模块
│       ├── src/main/java/com/lune/
│       │   ├── config/           # Security、CORS、MyBatis-Plus 配置
│       │   ├── security/         # JWT 认证过滤器
│       │   ├── entity/           # 数据库实体
│       │   ├── dto/              # 请求/响应 DTO
│       │   ├── mapper/           # MyBatis-Plus Mapper
│       │   ├── service/          # 业务逻辑
│       │   └── controller/       # REST 控制器
│       └── src/main/resources/
│           ├── application.yml           # 主配置（环境变量参数化）
│           ├── application-dev.yml       # 开发环境
│           ├── application-prod.yml      # 生产环境
│           └── application-local.yml.template  # 本地配置模板
├── lune-ui/                      # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/                  # Axios 封装 + API 函数
│   │   ├── router/               # Vue Router 路由
│   │   ├── stores/               # Pinia 状态管理
│   │   ├── layout/               # 布局组件
│   │   ├── views/                # 页面组件
│   │   └── assets/               # 样式、字体
│   └── public/assets/            # 静态资源
├── docker/                       # Docker 配置
│   ├── backend/Dockerfile        # 后端多阶段构建
│   ├── frontend/Dockerfile.dev   # 前端开发镜像
│   ├── nginx/                    # Nginx 配置 + 生产 Dockerfile
│   ├── mysql/                    # MySQL 配置 + 初始化 SQL
│   ├── redis/                    # Redis 配置
│   └── fonts/                    # 自托管字体
├── docker-compose.dev.yml        # 开发环境编排
├── docker-compose.prod.yml       # 生产环境编排
├── .env.template                 # 生产环境变量模板
├── .env.local.template           # 本地开发环境变量模板
├── deploy.sh                     # 一键部署脚本
├── backup.sh                     # 数据库备份脚本
└── Makefile                      # 便捷命令
```

---

## 🚀 部署指南

> 📘 **完整企业级部署文档（含 ICP 备案后接入域名 / HTTPS / CDN / OSS、资源预算、运维排查）见 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)**
>
> 🌐 **当前生产环境部署报告（含服务器架构图 / 资源限制 / Phase 2-5 增量路线图）见 [docs/SERVER-DEPLOYMENT.md](docs/SERVER-DEPLOYMENT.md)**
>
> 演示站点已部署在腾讯云 2C4G 5M 服务器（http://111.231.14.63），资源受限环境下的最佳实践参考。

### 方式一：Docker 部署（推荐）

适用于服务器生产环境，一键部署全部服务（Nginx + Spring Boot + MySQL + Redis）。

#### 前置条件

- 服务器操作系统：Ubuntu / Debian / CentOS
- 推荐配置：2核4G 以上（本方案已针对 2C4G + 5M 带宽调优）
- 需要开放端口：`80`（HTTP）、备案后 `443`（HTTPS）

#### 安全特性（已内置）

- 🔒 接口限流（Nginx + 后端双层，防爬虫/防爆破）
- 🔐 登录失败锁定（连续 5 次失败锁定 15 分钟）
- 🛡️ 完整安全响应头 + CSP + 存储型 XSS 过滤
- 🚫 生产环境关闭 sourcemap + 禁用调试快捷键（合理加固）
- 📦 文件存储抽象，本地 / OSS 一键切换（预留 CDN 接口）

#### 部署步骤

```bash
# 1. SSH 登录服务器
ssh user@your-server-ip

# 2. 安装 git（如未安装）
sudo apt-get install -y git

# 3. 克隆项目
git clone https://github.com/ztcools/Lune.git /opt/lune
cd /opt/lune

# 4. 一键部署
bash deploy.sh --prod
```

部署脚本会自动完成：
- 检测并安装 Docker + Docker Compose
- 自动生成 `.env` 环境变量文件（含随机安全密码）
- 构建 Docker 镜像
- 启动所有服务并等待健康检查通过
- 输出访问地址和默认管理员账号

```bash
# 查看服务状态
make status

# 查看日志
make prod-logs

# 重启服务
make restart
```

#### 首次登录

- 地址：`http://你的服务器IP`
- 后台：`http://你的服务器IP/admin/login`
- 默认管理员：`admin` / `admin123`
- ⚠️ **首次登录后请立即修改密码！**

#### 配置 SSL / HTTPS（ICP 备案后）

备案完成后，安装 Certbot 并配置 SSL：

```bash
sudo apt-get install -y certbot
sudo certbot certonly --standalone -d your-domain.com
# 修改 docker/nginx/nginx.prod.conf 添加 SSL 证书路径
make restart
```

---

### 方式二：本地开发（Docker Compose）

适用于本地开发调试，支持前端 HMR 热更新。

#### 前置条件

- Docker + Docker Compose
- Git

#### 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/ztcools/Lune.git
cd Lune

# 2. 初始化本地环境
make setup-dev
# 按提示编辑 .env.local 填入本地开发凭据

# 3. 下载字体（首次运行，可选）
bash docker/fonts/download-fonts.sh

# 4. 启动开发环境
make dev
```

#### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | `http://localhost:8080` | 通过 Nginx 代理访问 |
| Vite HMR | `http://localhost:5173` | Vite 开发服务器（热更新） |
| 后端 API | `http://localhost:8081/api` | Spring Boot REST API |
| MySQL | `localhost:3306` | 数据库 |
| Redis | `localhost:6379` | 缓存 |

#### 热更新说明

- **前端**：修改 `lune-ui/src/` 下任何文件，Vite HMR 自动刷新浏览器
- **后端**：修改 Java 代码后，运行 `make restart-backend` 重新构建

#### 常用命令

```bash
make dev           # 启动开发环境
make dev-logs      # 查看日志
make down          # 停止所有容器
make clean         # 停止并删除数据卷
make status        # 查看容器状态
make shell-backend # 进入后端 Shell
make shell-db      # 进入 MySQL
```

---

### 方式三：裸机运行

适用于没有 Docker 的环境。

#### 环境要求

Java 17 · Maven 3.8+ · Node.js 18+ · MySQL 8.0 · Redis

#### 启动步骤

```bash
# 1. 初始化数据库
mysql -u root -p < lune-server/lune-web/src/main/resources/sql/lune.sql

# 2. 配置环境变量
# 复制并编辑配置文件
cp lune-server/lune-web/src/main/resources/application-local.yml.template \
   lune-server/lune-web/src/main/resources/application-local.yml

# 3. 设置环境变量（或编辑 application-local.yml）
export DB_USERNAME=root
export DB_PASSWORD=your-password
export JWT_SECRET=your-jwt-secret

# 4. 启动后端
cd lune-server/lune-web
mvn spring-boot:run -Dspring-boot.run.profiles=local
# → http://localhost:8081

# 5. 启动前端（新终端）
cd lune-ui
npm install
npm run dev
# → http://localhost:5173
```

---

## 📋 运维指南

### 数据库备份

```bash
# 手动备份
make backup
# 备份文件: ./backups/lune_YYYYMMDD_HHMMSS.sql.gz

# 设置自动备份（每天凌晨 3:00）
bash backup.sh --cron

# 恢复备份
make restore FILE=backups/lune_20260101_030000.sql.gz
```

### 查看日志

```bash
make prod-logs       # 所有生产日志
make backend-logs    # 仅后端
make nginx-logs      # 仅 Nginx
```

### 更新部署

```bash
cd /opt/lune
git pull
make prod         # 重新构建并启动
```

### 环境变量说明

| 变量 | 说明 | 必填 |
|------|------|------|
| `DB_ROOT_PASSWORD` | MySQL root 密码 | ✅ |
| `DB_USERNAME` | 数据库用户名 | ✅ |
| `DB_PASSWORD` | 数据库密码 | ✅ |
| `JWT_SECRET` | JWT 签名密钥（64位随机字符串） | ✅ |
| `REDIS_PASSWORD` | Redis 密码 | ❌ |
| `MAIL_HOST` | SMTP 服务器地址 | ❌ |
| `MAIL_USERNAME` | 邮箱账号 | ❌ |
| `MAIL_PASSWORD` | 邮箱授权码 | ❌ |
| `NGINX_PORT` | Nginx 对外端口 | ❌（默认 80） |
| `ADMIN_DEFAULT_PASSWORD` | 默认管理员初始密码 | ❌（默认 admin123） |

---

## 📄 License

MIT
