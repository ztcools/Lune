# Lune 部署指南（企业级 · 2核4G · 5M带宽）

> 本文档覆盖从全新服务器到生产可用的完整流程，以及 ICP 备案下来后接入域名 / HTTPS / CDN / OSS 的步骤。

---

## 目录

- [一、服务器要求](#一服务器要求)
- [二、首次部署（5 分钟）](#二首次部署5-分钟)
- [三、部署后必做](#三部署后必做)
- [四、ICP 备案下来之后](#四icp-备案下来之后)
- [五、接入 CDN / OSS](#五接入-cdn--oss)
- [六、资源占用与性能预算](#六资源占用与性能预算)
- [七、运维常用命令](#七运维常用命令)
- [八、故障排查](#八故障排查)

---

## 一、服务器要求

| 项目 | 规格 | 说明 |
|------|------|------|
| CPU | 2 核 | 你的 2C 已满足 |
| 内存 | 4 GB | 你的 4G 已满足（见第六节预算） |
| 带宽 | 5 Mbps | 你的 5M 已满足；接入 CDN 后图片/视频流量走 CDN，源站带宽压力骤降 |
| 磁盘 | ≥ 20 GB | 系统 + Docker 镜像 + 数据库 + 上传文件 |
| 系统 | Ubuntu 22.04 / Debian 11+ | 其他发行版同理 |

**安全组（云防火墙）放行端口**：`80`（HTTP）、`443`（HTTPS，备案后）、`22`（SSH，建议改端口并仅允许你的 IP）。**不要对外暴露** 3306 / 6379 / 8081。

---

## 二、首次部署（5 分钟）

> 一键脚本会自动安装 Docker、生成随机密码、拉取镜像、建表、填种子数据。

```bash
# 1. 克隆代码（或上传项目到服务器）
git clone <你的仓库地址> lune && cd lune

# 2. 一键部署（生产模式）
sudo bash deploy.sh --prod
```

脚本完成后访问 `http://服务器IP` 即可。默认管理员 `admin / admin123`（**立即登录后台修改**）。

### 部署脚本做了什么

1. 检测并安装 Docker / Docker Compose
2. 从 `.env.template` 生成 `.env`，**自动填入随机强密码**（数据库、JWT）
3. 构建并启动 4 个容器：`nginx`(80) / `backend`(8081) / `mysql` / `redis`
4. 自动执行 `sql/migration-*.sql` 建表
5. 首次自动写入管理员、分类、站点配置、简历/项目演示数据

> 如需填充完整演示内容（文章/随笔/记录/树洞/日记/许愿等），执行一次：
> ```bash
> docker exec -i lune-mysql mysql -uroot -p$(grep DB_ROOT_PASSWORD .env | cut -d= -f2) lune \
>   < lune-server/lune-web/src/main/resources/sql/seed-data.sql
> ```

---

## 三、部署后必做

1. **改管理员密码**：后台 → 用户管理 → 修改 admin 密码。
2. **配置备份**：`bash backup.sh --cron` 会设置每天凌晨 3 点自动备份数据库到 `backups/`，保留 7 天。建议把 `backups/` 定期同步到异地（对象存储/另一台机器）。
3. **SSH 加固**：改默认端口、禁用密码登录改用密钥、安装 `fail2ban`。
4. **确认防火墙**：仅放行 80/443/22，其余端口关闭。

---

## 四、ICP 备案下来之后

备案号下来后，按以下顺序接入域名与 HTTPS。

### 4.1 填写备案号与域名

**备案号**：登录后台 → **设置 → 基础信息 → ICP备案号** 填入 `京ICP备XXXXXXXX号`，
保存后立即在 Landing 页脚显示（存 `site_config.beian_icp`，不需要重启容器）。
> 备案号不是环境变量。`.env` 里的 `DOMAIN` 也没有任何服务读取，它只是给你
> 自己留个记录 —— 域名要手动写进 nginx 配置和 certbot 命令（见 4.2 / 4.3）。

**域名解析**：到 DNS 服务商把 `blog.example.com` 的 **A 记录** 指向 `111.231.14.63`，
用 `dig +short blog.example.com` 确认生效后再申请证书（证书校验要求域名已解析）。

### 4.2 申请免费 HTTPS 证书（Let's Encrypt）

80 端口被 nginx 容器占着，`certbot --standalone` 会起自己的临时服务器抢 80 端口，
必然报 `Address already in use`。两条可行路线，选 **webroot**（不中断服务）：

```bash
sudo apt install -y certbot

# webroot 校验：certbot 把校验文件写到宿主机目录，nginx 容器挂载同一目录对外提供
sudo mkdir -p /opt/lune/certbot-webroot
# → 先按 4.3 第 1 步放开 certbot-webroot 挂载并重启 nginx，再执行下面这条
sudo certbot certonly --webroot -w /opt/lune/certbot-webroot -d blog.example.com

# 证书位置（宿主机）：
#   /etc/letsencrypt/live/blog.example.com/fullchain.pem
#   /etc/letsencrypt/live/blog.example.com/privkey.pem
```

> 备选：`docker compose -f docker-compose.prod.yml stop nginx` 后用 `--standalone`
> 申请，成功再 `start nginx` —— 简单但有约 1 分钟停机。

### 4.3 启用 HTTPS

1. **改 compose**（`/opt/lune/docker-compose.prod.yml`）：把 `nginx` 服务下预留的三行
   注释放开 —— `"443:443"`、`/etc/letsencrypt:/etc/nginx/ssl:ro`、
   `./certbot-webroot:/var/www/certbot`，然后 `up -d nginx` 让 webroot 生效。
2. **改 nginx 配置**（`docker/nginx/nginx.prod.conf`，在**本地**改）：放开底部
   **HTTPS Server 预留块**，把 `your-domain.com` 换成真实域名，并把上方 HTTP server
   改成 301 跳转 + 保留 `/.well-known/acme-challenge/`（预留块注释里有现成片段）。
3. **重新出镜像**：nginx 配置是 `COPY` 进镜像的，改完必须重建。按
   [SERVER-DEPLOYMENT.md](SERVER-DEPLOYMENT.md) 的方式**本地** build → `docker save`
   → scp → 服务器 `docker load`（服务器上不 build，2C4G 跑前端构建会 OOM）。
4. **开 HSTS**：`/opt/lune/.env` 设 `HSTS_ENABLED=true`，`up -d backend` 重启后端。
   顺序很重要 —— 证书没生效就下发 HSTS，浏览器会记住「只走 HTTPS」整整一年。
5. **放行 443**：云控制台安全组加 443 入站规则，否则证书配好了外网照样连不上。

### 4.4 证书自动续期

```bash
# certbot 自带 systemd timer，先确认续期本身能跑通：
sudo certbot renew --dry-run

# 续期后 nginx 容器不会自动重载新证书，挂个 deploy-hook：
sudo tee /etc/letsencrypt/renewal-hooks/deploy/reload-lune-nginx.sh >/dev/null <<'EOF'
#!/bin/sh
docker exec lune-nginx nginx -s reload
EOF
sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-lune-nginx.sh
```

---

## 五、接入 CDN / OSS

源站 5M 带宽是瓶颈，**图片/视频/静态资源上 CDN 后体验质变**。

### 5.1 接入 OSS（对象存储）

代码已预留无缝切换接口（`StorageService` 抽象 + `OssStorageService` 占位）。

1. 开通对象存储（阿里云 OSS / 腾讯云 COS / 七牛任选）。
2. 在 `lune-server/lune-web/pom.xml` 加入对应 SDK 依赖（以阿里云为例）：
   ```xml
   <dependency>
     <groupId>com.aliyun.oss</groupId>
     <artifactId>aliyun-sdk-oss</artifactId>
     <version>3.17.4</version>
   </dependency>
   ```
3. 实现 `OssStorageService.store()`（文件内有详细注释与示例代码）。
4. `.env` 配置：
   ```bash
   STORAGE_TYPE=oss
   OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
   OSS_BUCKET=your-bucket
   OSS_ACCESS_KEY=xxx
   OSS_SECRET_KEY=xxx
   OSS_CDN_DOMAIN=https://cdn.example.com   # 配了 CDN 才填
   ```
5. 重启后端即可，业务层零改动。

### 5.2 接入 CDN

- **静态资源/图片**：把 OSS 绑定 CDN 加速域名，`OSS_CDN_DOMAIN` 填 CDN 域名，资源 URL 自动走 CDN。
- **整站 CDN**（可选）：将域名 CNAME 到 CDN 厂商，回源指向服务器。注意 API（`/api`）路径设置**不缓存**。

---

## 六、资源占用与性能预算

针对 2C4G 已做的调优与内存预算：

| 服务 | 内存上限 | 说明 |
|------|----------|------|
| Nginx | ~50 MB | 静态 + 反代，极轻 |
| Spring Boot | 512 MB（JVM `-Xmx512m`） | `Dockerfile` 已限制，G1GC |
| MySQL | ~450 MB | `innodb_buffer_pool_size=256M` 已瘦身 |
| Redis | 128 MB | `maxmemory 128mb` + LRU 淘汰 |
| 系统 + Docker | ~500 MB | 预留 |
| **合计** | **≈ 1.7 GB** | 剩余 ~2GB 余量，安全 |

**性能优化已内置**：

- Nginx：Gzip、静态资源强缓存（hash 1 年）、HTTP/1.1 keepalive 长连接、API 限流。
- 后端：连接池（HikariCP 10）、原子计数 SQL、Redis 限流/黑名单。
- 前端：路由懒加载、three.js/Element Plus 单独分包按需加载、生产去 console/sourcemap、图片走 CDN 后秒开。
- 数据库：慢查询日志已开（`long_query_time=2`），便于发现慢 SQL。

**5M 带宽提示**：未接 CDN 前，首屏图片较多时建议开启后台"图片压缩/裁剪"或使用 OSS+CDN；文字类接口响应极小，5M 完全够用。

---

## 七、运维常用命令

```bash
# 查看服务状态
docker compose -f docker-compose.prod.yml ps

# 查看后端日志（实时）
docker logs -f lune-backend

# 重启某个服务
docker compose -f docker-compose.prod.yml restart backend

# 更新代码后重新部署
git pull && sudo bash deploy.sh --prod

# 手动备份数据库
bash backup.sh

# 进入 MySQL
docker exec -it lune-mysql mysql -uroot -p
```

---

## 八、故障排查

| 现象 | 排查 |
|------|------|
| 访问 502 | `docker logs lune-backend` 看后端是否起来了；`docker ps` 确认 backend healthy |
| 后端起不来 | 看日志，多为数据库连接失败 → 确认 `.env` 的 `DB_PASSWORD` 与 MySQL 容器一致 |
| 图片不显示 | 确认 `upload_data` volume 挂载；接入 OSS 后确认 `STORAGE_TYPE` 与 OSS 配置 |
| 接口 429 | 触发限流，正常防护；如需调整改 `RateLimitFilter` / nginx `limit_req` |
| 内存不足 OOM | `docker stats` 看占用；必要时再降 MySQL `innodb_buffer_pool_size` |

---

## 九、换设备 / 重新 clone 后跑起来

项目不依赖任何本机状态，新设备从零启动仅需：

```bash
git clone <仓库地址> lune && cd lune

# 本地开发（自动建 .env.local、拉起 nginx+backend+frontend+mysql+redis）
make dev
# 打开 http://localhost:8080

# 需要演示数据再执行一次（幂等，可重复跑）
docker exec -i lune-mysql-dev mysql -uroot -p$(grep DB_ROOT_PASSWORD .env.local | cut -d= -f2) lune \
  < lune-server/lune-web/src/main/resources/sql/seed-data.sql
```

> 说明：数据库内容、上传文件都在 Docker volume 里，**不会**随 git 迁移，新环境是空库 + 自动初始化（管理员/分类/配置/简历）+ 可选种子数据，这通常正是想要的"干净起步"。

---

## 十、局域网 / 手机同网段访问

架构**天然支持**局域网访问：nginx/后端端口绑定 `0.0.0.0`，前端走相对路径 `/api`（自动适配任意 IP/域名），CORS 已放开。

### 普通 Linux / 物理机 / 云主机

启动后，手机与电脑连同一 WiFi，浏览器直接访问：

```
http://<电脑局域网IP>:8080        # 开发
http://<电脑局域网IP>            # 生产(80端口)
```

查局域网 IP：`ip addr`（Linux）或 `ipconfig`（Windows）。若打不开，基本是**防火墙拦了 8080 端口**，放行即可：

```bash
sudo ufw allow 8080/tcp        # Ubuntu
# 或 sudo firewall-cmd --add-port=8080/tcp --permanent && sudo firewall-cmd --reload
```

### ⚠️ WSL2 特殊情况（Windows 上的 WSL）

WSL2 是 NAT 网络，**Windows 宿主机和手机默认都访问不到 WSL 内部 IP**。手机要访问，需在 **Windows PowerShell(管理员)** 做端口代理：

```powershell
# 查 WSL IP：wsl hostname -I
netsh interface portproxy add v4tov4 listenport=8080 listenaddress=0.0.0.0 connectport=8080 connectaddress=<WSL的IP>
# Windows 防火墙放行 8080
New-NetFirewallRule -DisplayName "WSL-Lune" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

之后手机访问 `http://<Windows的局域网IP>:8080`。WSL 重启后 IP 会变，需更新 connectaddress。
**建议**：开发机若非 WSL，直接用物理机/云主机可免去这层代理。

---

> 遇到问题先看 `docker logs`，90% 的部署问题日志里都有答案。
