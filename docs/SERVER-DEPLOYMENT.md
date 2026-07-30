# Lune 生产环境部署报告

> **服务器**: 111.231.14.63 (腾讯云 2C4G 5M)
> **部署时间**: 2026-07-30
> **部署状态**: ✅ 已完成，可正常访问
> **访问地址**: http://111.231.14.63
> **后台管理**: http://111.231.14.63/admin (`admin` / `admin123`)

---

## 一、服务器环境

| 项目 | 配置 |
|------|------|
| **操作系统** | Ubuntu 24.04 (Linux 6.8.0-124-generic) |
| **CPU** | 2 核 |
| **内存** | 4 GB (3.6 GiB 可用) |
| **磁盘** | 59 GB 总量，51 GB 可用 |
| **带宽** | 5 Mbps |
| **网络限制** | ⚠️ Docker Hub 直连不通（`registry-1.docker.io` 超时），必须走下方镜像；GitHub 可达但握手约 8s；`fonts.googleapis.com` 解析到国内 IPv6、拿不到真实字体文件 —— 故字体/图片/音乐全部自托管 |
| **Docker** | 29.1.3 (apt 安装) |
| **Docker Compose** | v2.27.0（用户级安装于 `~/.docker/cli-plugins/`） |
| **Registry Mirror** | docker.1ms.run / docker.xuanyuan.me / hub.rat.dev |

---

## 二、部署架构

```
                            ┌─────────────────────────────────────┐
                            │  腾讯云 Ubuntu 24.04 (111.231.14.63)│
                            │                                     │
   用户 ──HTTP(80)────►    │  ┌──────────────────────────────┐  │
                            │  │  lune-nginx                  │  │
                            │  │  image: lune-nginx:latest    │  │
                            │  │  ports: 0.0.0.0:80 -> 80     │  │
                            │  │  - 内嵌前端 dist             │  │
                            │  │  - 反代 /api -> backend      │  │
                            │  │  - 反代 /upload -> volume    │  │
                            │  └────────┬─────────────────────┘  │
                            │           │                         │
                            │           ▼ localhost:8081          │
                            │  ┌──────────────────────────────┐  │
                            │  │  lune-backend                │  │
                            │  │  image: lune-backend:latest  │  │
                            │  │  ports: 127.0.0.1:8081       │  │
                            │  │  JAVA_OPTS: -Xms256m -Xmx512m│  │
                            │  │  内存限制: 768M              │  │
                            │  └────┬─────────────────┬───────┘  │
                            │       │                 │           │
                            │       ▼                 ▼           │
                            │  ┌─────────┐      ┌──────────┐     │
                            │  │ lune-   │      │ lune-    │     │
                            │  │ mysql   │      │ redis    │     │
                            │  │ 8.0     │      │ 7-alpine │     │
                            │  │ 768M限  │      │ 256M限   │     │
                            │  │ 256MB   │      │ AOF持久化│     │
                            │  │ buffer  │      │          │     │
                            │  └─────────┘      └──────────┘     │
                            │                                     │
                            │  volumes:                           │
                            │   mysql_data, redis_data,           │
                            │   upload_data                       │
                            └─────────────────────────────────────┘
```

### 部署目录
```
/opt/lune/                       # 项目部署根目录（owner: ubuntu）
├── docker-compose.yml           # 主配置文件（从 docker-compose.prod.yml 复制）
├── .env                         # 环境变量（chmod 600，含 DB/JWT/Redis 密码）
└── (容器内自动创建的 volumes)
    ├── mysql_data               # MySQL 数据
    ├── redis_data               # Redis AOF 持久化
    └── upload_data              # 用户上传文件
```

### 临时部署目录
```
/tmp/lune-deploy/                # 部署时的临时文件（可清理）
├── lune-images.tar              # 本地构建的镜像（132MB）
├── lune-data.sql / lune-data-clean.sql  # 数据库迁移SQL
├── docker-compose.prod.yml      # compose 模板
├── .env                         # 环境变量源
└── deploy.sh                    # 一键部署脚本
```

---

## 三、部署方式（无外网服务器专用）

由于服务器只能访问国内网络，**采用本地构建镜像 + scp 上传**的方式：

```bash
# 1. 本地构建（含前端 dist 内嵌到 nginx 镜像）
docker compose -f docker-compose.prod.yml --env-file .env.local build

# 2. 导出镜像
docker save lune-backend:latest lune-nginx:latest -o /tmp/lune-deploy/lune-images.tar

# 3. 上传到服务器
scp /tmp/lune-deploy/* ubuntu@111.231.14.63:/tmp/lune-deploy/

# 4. 服务器上加载 + 启动
ssh ubuntu@111.231.14.63
docker load -i /tmp/lune-deploy/lune-images.tar
docker pull mysql:8.0 && docker pull redis:7-alpine  # 国内镜像加速
cd /opt/lune
docker compose up -d
```

---

## 四、数据库迁移

| 项目 | 值 |
|------|---|
| **数据库名** | `lune` |
| **字符集** | utf8mb4 / utf8mb4_unicode_ci |
| **表数量** | 18 张 |
| **演示数据** | 5 文章 / 5 随笔 / 4 记录 / 12 弹幕 / 9 评论 / 4 日记 / 6 许愿 / 1 家页 / 8 标签 / 7 分类 |
| **管理员账号** | `admin` / `admin123`（BCrypt 加密存储） |

**迁移方式**：本地 `mysqldump` → 清理警告行 → 服务器 `mysql < dump.sql` 一次性导入。

---

## 五、资源限制配置（2C4G 优化）

| 容器 | 内存限制 | 预留内存 | 备注 |
|------|---------|---------|------|
| lune-nginx | 128 M | - | 静态资源 + 反代 |
| lune-backend | 768 M | 384 M | JVM: -Xms256m -Xmx512m |
| lune-mysql | 768 M | 256 M | innodb_buffer_pool=256M |
| lune-redis | 256 M | 64 M | AOF 持久化 |
| **合计** | **1920 M** | | 预留 2 GB 给系统 |

**日志管理**: 所有容器 `max-size: 10m, max-file: 3-5`，避免磁盘被日志撑爆。

---

## 六、网络访问说明

### 当前状态（HTTP）
- **入口**: http://111.231.14.63 (80 端口，nginx 直接对外）
- **后端**: 仅监听 `127.0.0.1:8081`（通过 nginx 反代访问）
- **数据库**: 仅监听 `127.0.0.1:3306`（容器网络内部访问）
- **Redis**: 仅监听 `127.0.0.1:6379`（容器网络内部访问）

### 安全特性（已启用）
- ✅ Spring Security + JWT
- ✅ RateLimitFilter（auth 5r/m，写 20/min，读 120/min，Redis 滑动窗口）
- ✅ LoginAttemptService（连续失败 5 次锁定 15 分钟）
- ✅ SecurityHeadersFilter（CSP / X-Frame-Options / Permissions-Policy）
- ✅ XssSanitizer（评论/树洞纯文本字段服务端转义）
- ✅ ResourceServiceImpl（文件类型白名单 + 50MB 限制 + 拒绝 SVG）
- ✅ Nginx 限流区 + 屏蔽恶意扫描路径
- ✅ 生产关 sourcemap + terser drop_console/debugger

---

## 七、后续增量部署路线图（ICP 备案完成后）

### Phase 2: 域名 + HTTPS
**触发条件**: ICP 备案完成 + 域名解析到 111.231.14.63

**完整步骤见 [DEPLOYMENT.md 第四章](DEPLOYMENT.md#四icp-备案下来之后)**（唯一权威版本，
这里只列本机特有的注意点，避免两份步骤各写一半后互相矛盾）：

- **备案号填后台**，不是填 `.env` —— 后台「设置 → 基础信息 → ICP备案号」，
  存 `site_config.beian_icp`，保存即在 Landing 页脚生效。
- **不要用 `certbot --nginx`**：那个插件改的是宿主机的 nginx，本项目 nginx 跑在容器里，
  配置还是 `COPY` 进镜像的。用 `certbot certonly --webroot`（见 DEPLOYMENT.md 4.2）。
- **改完 nginx 配置不能只 `restart nginx`**：配置在镜像里，必须本地重建镜像 →
  `docker save` → scp → `docker load` → `up -d nginx`。
- **`HSTS_ENABLED=true` 最后再开**，且必须在 https 确认能打开之后：
  HTTP 站点下发 HSTS 会被浏览器记住一年，等于自己把站点锁死。
- 预留位已就位：`docker-compose.prod.yml` 的 nginx 服务有 `443` 与证书挂载的注释行，
  `nginx.prod.conf` 底部有 443 server 预留块（`include lune-common.conf`，
  不需要复制 location），`application-prod.yml` 已开 `forward-headers-strategy: framework`。
- 云控制台安全组记得放行 **443**。

### Phase 3: OSS 对象存储（图片/视频外置）
**触发条件**: 用户量增长或上传量增大，服务器 5M 带宽吃紧

1. **开通阿里云 OSS / 腾讯云 COS**:
   - 创建 bucket: `lune-uploads`
   - 配置 CDN 加速域名
   - 获取 AccessKey / SecretKey

2. **修改 `.env`**:
   ```env
   STORAGE_TYPE=oss
   OSS_ENDPOINT=oss-cn-guangzhou.aliyuncs.com
   OSS_BUCKET=lune-uploads
   OSS_ACCESS_KEY=xxx
   OSS_SECRET_KEY=xxx
   OSS_CDN_DOMAIN=cdn.your-domain.com
   ```

   > 这些变量已由 `docker-compose.prod.yml` 透传进 backend 容器（`STORAGE_TYPE` /
   > `OSS_*` / `HSTS_ENABLED` / `ADMIN_DEFAULT_PASSWORD`）。在此之前 compose 只传到
   > `JAVA_OPTS`，`.env` 里改这些 key 是不生效的 —— 若你的服务器上是旧版 compose 文件，
   > 记得一并更新。

3. **代码已就绪** `OssStorageService.java` 已预留接口，仅需实现 `upload()` 方法（参考 `LocalStorageService.java`)

4. **数据迁移**: 把现有 `/upload` 目录文件同步到 OSS（用 `ossutil` 或 `coscmd`)

5. **重启 backend**: `docker compose up -d --force-recreate backend`

### Phase 4: CDN 静态资源加速
**触发条件**: 全国访问速度慢

1. **开通 CDN**（腾讯云/阿里云）:
   - 加速域名：`cdn.your-domain.com`
   - 回源：`111.231.14.63:80`
   - 缓存规则：`/upload/*` 7 天，`/assets/*` 30 天

2. **修改 `vite.config.js`** 构建时指定 `base: 'https://cdn.your-domain.com/'`

3. **重新构建前端镜像**:
   ```bash
   docker compose -f docker-compose.prod.yml build nginx
   # 走 docker save / scp 流程更新镜像
   ```

4. **替换上传文件 URL**: 后台 `site_config` 中 `/upload/xxx` → `https://cdn.your-domain.com/upload/xxx`

### Phase 5: 监控告警（可选）
- **接入 Uptime Robot / 腾讯云监控** 监控 http://111.231.14.63
- **接入 Sentry** 前端异常上报
- **接入 Loki** 集中式日志收集（容器 logs → Loki）

---

## 八、常用运维命令

```bash
# 进入部署目录
cd /opt/lune

# 查看状态
docker compose ps

# 查看日志（实时）
docker compose logs -f backend
docker compose logs -f nginx
docker compose logs -f mysql

# 重启单个服务
docker compose restart backend

# 停止/启动全部
docker compose stop
docker compose up -d

# 清理未使用镜像（释放磁盘）
docker system prune -a

# 备份数据库
docker exec lune-mysql mysqldump -uroot -p$(grep DB_ROOT_PASSWORD .env | cut -d= -f2) lune | gzip > backup-$(date +%Y%m%d-%H%M%S).sql.gz

# 进入容器调试
docker exec -it lune-backend sh
docker exec -it lune-mysql mysql -uroot -p$(grep DB_ROOT_PASSWORD .env | cut -d= -f2) lune
```

---

## 九、故障排查

### 1. 网站访问 502
```bash
# 检查 backend 是否健康
docker compose ps
docker compose logs backend --tail 50
# 通常是 MySQL 连接问题或 JVM 内存不足
```

### 2. 上传图片 404
```bash
# 检查 upload volume 是否挂载
docker inspect lune-nginx | grep upload
docker inspect lune-backend | grep upload
```

### 3. 数据库连不上
```bash
# 检查 MySQL 健康
docker exec lune-mysql mysqladmin ping -h localhost -uroot -p$(grep DB_ROOT_PASSWORD .env | cut -d= -f2)
# 查看慢查询日志
docker exec lune-mysql cat /var/log/mysql/slow.log
```

### 4. 磁盘满
```bash
df -h
# 清理 Docker 垃圾
docker system df
docker system prune -a
# 清理日志
docker compose logs --no-log-prefix backend > /dev/null
```

---

## 十、下次会话 Agent 接手提示

如果你是新接手这个项目的 Agent，请按以下顺序操作：

1. **读这份文档**了解当前部署状态
2. **登录服务器**: `ssh ubuntu@111.231.14.63`
3. **进入部署目录**: `cd /opt/lune`
4. **查看当前服务**: `docker compose ps`
5. **检查日志**: `docker compose logs --tail 100 backend`
6. **按需推进 Phase 2-5**（HTTPS / OSS / CDN / 监控）

**不要做的事**:
- ❌ 直接修改容器内文件（容器重启即丢失）
- ❌ 删除 `mysql_data` / `redis_data` / `upload_data` volume
- ❌ 在服务器上 `git clone`（服务器无外网）
- ❌ 在服务器上 `docker build`（应该本地构建后上传）

**该做的事**:
- ✅ 本地构建镜像 → docker save → scp 上传 → docker load
- ✅ 配置文件改动通过 `/opt/lune/.env` 或 `docker-compose.yml`
- ✅ 数据库改动通过 migration SQL（参考 `lune-server/lune-web/src/main/resources/sql/`)

---

## 十一、部署验证清单

- [x] Docker / docker compose 安装并配置国内镜像
- [x] 本地构建生产镜像（backend + nginx)
- [x] 镜像上传到服务器并加载
- [x] MySQL / Redis 基础镜像拉取
- [x] 数据库初始化和演示数据导入
- [x] 所有容器启动且 healthy
- [x] 80 端口可访问（外网）
- [x] 所有 API 端点 200
- [x] 资源限制已配置（JVM/MySQL/Redis)
- [x] 日志轮转已配置
- [ ] HTTPS / 域名（等 ICP)
- [ ] OSS 对象存储（等需要）
- [ ] CDN 加速（等需要）

---

**部署完成时间**: 2026-07-30
**操作人**: Claude Code Agent
**版本**: v1.0.0 (commit 88aa89e)
