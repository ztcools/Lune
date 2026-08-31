# Lune CI/CD —— 自动测试与部署

> 本文档说明项目的持续集成（CI）与持续部署（CD）流水线：代码如何被自动测试、
> 构建成镜像，并部署到无外网的腾讯云生产服务器。

---

## 一、总览

```
  push / PR
      │
      ▼
┌───────────────────────────────────────────────┐
│  CI（.github/workflows/ci.yml，GitHub 托管）  │
│  · 后端 lune-server：单元测试 + Testcontainers │
│    集成测试（真实 MySQL + Redis 容器）        │
│  · Agent lune-agent：单元测试                 │
│  · 前端 lune-ui：Vitest 单测/组件测试 + 生产构建│
└───────────────────────────────────────────────┘
      │
      │ push 到 main
      ▼
┌───────────────────────────────────────────────┐
│  CD（.github/workflows/deploy.yml，自托管）   │
│  本地 Runner：                                │
│  · docker build 三套镜像（backend/nginx/agent）│
│  · docker save + gzip                          │
│  · scp 到服务器 /opt/lune/deploy-tmp/          │
│  · 服务器 bash deploy-server.sh：              │
│      docker load → 替换 compose → 校验         │
│      → 迁移 → up -d → 健康检查 → 清理          │
└───────────────────────────────────────────────┘
```

三个 workflow 文件：

| 文件 | 触发 | 说明 |
|------|------|------|
| `.github/workflows/ci.yml` | push 到 `main` / PR / 手动 | 自动测试（后端 + Agent + 前端） |
| `.github/workflows/deploy.yml` | push 到 `main` / 手动 | 构建镜像并部署到生产服务器 |
| `.github/workflows/e2e.yml` | 手动（`workflow_dispatch`） | 可选 Playwright E2E 冒烟 |

---

## 二、自动化测试（本次新增）

项目此前**零测试**，本次补齐了三端测试，并让 CI 强制跑通：

### 后端 `lune-server`（28 个用例）

| 测试类 | 类型 | 覆盖 |
|--------|------|------|
| `XssSanitizerTest` | 单元 | XSS 转义 / 长度截断 / null |
| `JwtTokenProviderTest` | 单元 | 签发/解析/校验/短密钥报错 |
| `SecurityUtilsTest` | 单元 | 从 SecurityContext 取当前用户 |
| `ResultTest` | 单元 | Result/PageResult/BusinessException 契约 |
| `WishServiceImplTest` | 单元(Mockito) | 点赞/取消点赞/登录校验/404 |
| `LuneIntegrationTest` | **集成** | Testcontainers 拉起 MySQL 8.0 + Redis 7，用 `sql/lune.sql` 建表，验证健康检查、公开接口、登录鉴权全链路 |

### Agent `lune-agent`（15 个用例）

`AgentExceptionTest` / `JwtTokenProviderTest` / `ChatMemoryTest` / `UserPreferenceTest`（Mock Redis）。

### 前端 `lune-ui`（37 个用例，Vitest + jsdom）

`utils/date|format|media|imageUrl` 纯函数、`LineIcon` 组件渲染、`user` store（登录/登出与 localStorage 同步）。

### 可选 E2E（Playwright）

`lune-ui/e2e/smoke.spec.js`：对站点做只读冒烟（首页/后台登录页可访问、可渲染）。默认指向生产站点，`workflow_dispatch` 手动触发。

---

## 三、本地运行测试

```bash
# 后端（含 Testcontainers 集成测试，需本机 Docker）
cd lune-server && mvn -B test

# Agent
cd lune-agent && mvn -B test

# 前端
cd lune-ui && npm test          # 单测/组件测试
cd lune-ui && npm run build     # 生产构建冒烟

# 可选 E2E（需先 npx playwright install --with-deps chromium）
cd lune-ui && E2E_BASE_URL=https://ztcools.com npm run test:e2e
```

---

## 四、CI 说明（`ci.yml`）

- 运行在 **GitHub 托管的 `ubuntu-latest`**（有外网、有 Docker），不占用本地 Runner。
- 三个 job 并行：`backend` / `agent` / `frontend`。
- `backend` job 里 `LuneIntegrationTest` 用 Testcontainers 拉取 `mysql:8.0`、`redis:7-alpine`，约需 1 分钟。
- Maven / npm 均启用依赖缓存（`actions/setup-java` 的 `cache: maven`、`actions/setup-node` 的 `cache: npm`）。

---

## 五、CD 说明（`deploy.yml` + `scripts/deploy-server.sh`）

### 为什么自托管 Runner

生产服务器**无外网**（Docker Hub 直连不通、GitHub 握手慢），此前一直是「本地构建镜像 →
`docker save` → `scp` → 服务器 `docker load`」的手工链路。CD 直接把这套已验证的链路搬进
**自托管 Runner（本地机器）**，不引入 registry，零额外基础设施。

### 部署链路（每次 push 到 `main`）

1. `docker build` 三套镜像：`lune-backend:latest`、`lune-nginx:latest`（内嵌前端 dist）、`lune-agent:latest`。
2. `docker save | gzip` 压缩导出到 `/tmp/lune-deploy/`。
3. `scp` 上传镜像包 + `docker-compose.server.yml` + `migration-*.sql` + `scripts/deploy-server.sh` 到服务器 `/opt/lune/deploy-tmp/`。
4. SSH 执行 `deploy-server.sh`（全程幂等）：
   - `docker load` 三套镜像；
   - 备份并替换 `docker-compose.yml`；
   - `docker compose config -q` 先校验再起；
   - 逐个执行 `migration-*.sql`（幂等）；
   - `docker compose up -d`；
   - 后端 `/api/actuator/health` 健康检查（30 次 × 2s）；
   - 清理临时镜像包 + 悬空镜像。

> 镜像标签固定 `:latest`，`up -d` 靠 **image ID 变化**触发容器重建（输出里应看到 `Recreate`）。

---

## 六、自托管 Runner 安装

在**本地那台有外网的机器**（能 SSH 到生产服务器、能跑 Docker 构建）上执行：

```bash
mkdir -p ~/actions-runner && cd ~/actions-runner
# 1. 从 GitHub 仓库 Settings → Actions → Runners → New self-hosted runner 复制下载命令
#    （Linux x64 版本），下载并解压 runner 包

# 2. 配置（按页面提示填入 token）
./config.sh --url https://github.com/ztcools/Lune --token <TOKEN>

# 3. 注册为服务，开机自启
sudo ./svc.sh install && sudo ./svc.sh start
```

前置条件（本地机器需满足）：

- Linux x64、Docker（当前用户已在 `docker` 组，`docker ps` 无需 sudo）；
- Maven 3.8+、JDK 17+、Node 20+；
- 基础镜像已在本地缓存：`node:20-alpine`、`maven:3.9-eclipse-temurin-17-alpine`、
  `eclipse-temurin:17-jre-alpine`、`nginx:1.27-alpine`、`mysql:8.0`、`redis:7-alpine`。
  若本地 Docker Hub 直连不通，请提前用镜像源 `docker pull` 这些镜像（或配置 daemon 镜像加速）；
- 能 SSH 到生产服务器（`ssh ubuntu@111.231.14.63`）。

> Runner 默认标签为 `self-hosted, linux, x64`。`deploy.yml` 用 `runs-on: self-hosted` 匹配；
> 若你有多台 Runner，建议给部署机加自定义标签（如 `lune`），并把 `deploy.yml` 的
> `runs-on` 改为 `[self-hosted, lune]`。

---

## 七、服务器 SSH 访问（无需 GitHub Secrets）

`deploy.yml` 不依赖 GitHub Secrets —— 部署机（自托管 Runner）通过 `~/.ssh/config`
里的别名 `lune-prod` 直连生产服务器。首次在部署机上执行一次即可：

```bash
mkdir -p ~/.ssh && chmod 700 ~/.ssh
cat >> ~/.ssh/config <<'EOF'
Host lune-prod
  HostName 111.231.14.63
  User ubuntu
  IdentityFile ~/.ssh/lune_ci_deploy
  IdentitiesOnly yes
  StrictHostKeyChecking accept-new
EOF
chmod 600 ~/.ssh/config
```

其中 `~/.ssh/lune_ci_deploy` 是专用部署密钥，其公钥需在服务器 `~/.ssh/authorized_keys` 里：

```bash
ssh ubuntu@111.231.14.63 'cat >> ~/.ssh/authorized_keys' < ~/.ssh/lune_ci_deploy.pub
```

> 为什么不用 Secrets：自托管 Runner 就运行在部署机本身，直接复用机器上的密钥
> 更简单也更安全（私钥不落 GitHub）。若未来把 Runner 迁到云端机器，再改回
> Secrets 方案也不迟。

---

## 八、首次启用 CD 前检查清单

1. ✅ 自托管 Runner 已安装并在线（仓库 Actions 页能看到 `Idle`）。
2. ✅ 部署机 `~/.ssh/config` 的 `lune-prod` 别名已配置（`ssh lune-prod` 可通）。
3. ✅ 服务器 `/opt/lune/.env` 已存在且含 `DB_ROOT_PASSWORD`（`deploy-server.sh` 迁移用到）。
4. ✅ 服务器 `/opt/lune/data/geoip/GeoLite2-City.mmdb` 已就位（缺失仅降级，不影响部署）。
5. ✅ 新迁移脚本已 `git add` 提交 —— **未提交的 `migration-*.sql` 不会出现在 Runner 的
   checkout 里，也就不会被部署**（当前工作区里 `migration-20260817-worklog.sql` 尚未提交）。
6. ⚠️ 生产数据库已有真实数据：`LuneIntegrationTest` 用的是临时容器，**不会**碰生产库，放心。

---

## 九、故障排查

| 现象 | 原因 / 处理 |
|------|-------------|
| `Could not find a valid Docker environment` / `client version ... too old` | Testcontainers 与 Docker 引擎版本不匹配。已用 1.21.4（支持 Docker API 1.40+）；若再出现请升级 `testcontainers.version` |
| 部署卡在 `docker build` | 基础镜像未缓存且本地拉不到 → 配 daemon 镜像加速或先手动 `docker pull` |
| `scp: No such file` | 某条 `migration-*.sql` 未提交 → `git add` 后再推 |
| 服务器 `up -d` 后容器没换镜像（显示 `Running` 而非 `Recreate`） | 镜像 ID 未变，检查 `docker save/load` 是否成功、标签是否一致 |
| 后端健康检查超时 | `ssh` 上服务器看 `docker compose --env-file .env logs backend` |
| 部署前想人工确认 | 改用 PR 流程：功能分支 → PR（CI 跑绿）→ 合并 main 自动部署 |
