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

```bash
vim .env
# 填入：
DOMAIN=blog.example.com        # 你的域名
BEIAN_ICP=京ICP备XXXXXXXX号     # 你的备案号
```

域名解析：到 DNS 服务商把 `blog.example.com` 的 **A 记录** 指向服务器公网 IP。

### 4.2 申请免费 HTTPS 证书（Let's Encrypt）

```bash
# 安装 certbot
sudo apt install -y certbot

# 申请证书（需先确保 80 端口可访问且域名已解析）
sudo certbot certonly --standalone -d blog.example.com

# 证书位置：
#   /etc/letsencrypt/live/blog.example.com/fullchain.pem
#   /etc/letsencrypt/live/blog.example.com/privkey.pem
```

### 4.3 启用 HTTPS

1. 把证书挂载进 nginx 容器：在 `docker-compose.prod.yml` 的 `nginx.volumes` 加：
   ```yaml
   - /etc/letsencrypt:/etc/nginx/ssl:ro
   ```
2. 打开 `docker/nginx/nginx.prod.conf` 底部 **HTTPS Server 预留块**，取消注释并把
   `your-domain.com` 全部替换为你的域名，把上方 HTTP server 改为 301 跳转。
3. `.env` 设置 `HSTS_ENABLED=true`。
4. 重启：`docker compose -f docker-compose.prod.yml up -d --build nginx backend`。

### 4.4 证书自动续期

```bash
# certbot 自带 systemd timer，验证：
sudo certbot renew --dry-run
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

> 遇到问题先看 `docker logs`，90% 的部署问题日志里都有答案。
