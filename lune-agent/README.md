# Lune Agent — AI 博客管理助手

独立 Spring Boot 微服务，通过 DeepSeek Function Calling 实现自然语言网站管理。

## Architecture

```
HTTP POST /api/admin/agent/chat
        │
        ▼
┌───────────────────┐
│  AgentController  │  提取 JWT token, userId
└───────┬───────────┘
        │
        ▼
┌───────────────────┐
│ AgentOrchestrator │  核心流水线
│                   │
│  Step 1: Memory   │──► ChatMemory.load(userId)
│  Step 2: Context  │──► System Prompt + History + User Msg
│  Step 3: LLM Call │──► DeepSeek /v1/chat/completions (with tools)
│  Step 4: Parse    │──► tool_calls? → Step 5 : text? → Step 6
│  Step 5: Execute  │──► ToolExecutor.execute() → LuneApiClient → lune-web
│  Step 6: Response │──► SSE stream (tool_call/tool_result/text/done)
│  Step 7: Save     │──► ChatMemory.save(userId)
└───────────────────┘
```

## Memory Mechanism

```
Redis Keys:
  agent:chat:{userId}:{sessionId}:{yyyy-MM-dd} → JSON array (max 100 messages)
  agent:context:{userId}                      → "true"/"false"
  agent:pref:{userId}                         → Hash（用户偏好，不过期）

Lifecycle:
  - Every message pushed to array
  - TTL = seconds until midnight (auto-clear at 00:00)
  - Context toggle via frontend "记忆" button
  - Manual clear via "清空" button
```

## Tools (28 Function Calls)

| Category | Tools |
|----------|-------|
| 文章 | create_article, publish_article, update_article, delete_article, list_articles |
| 随笔 | create_essay, delete_essay, list_essays |
| 记录 | create_record, delete_record, list_records |
| 树洞 | list_treeholes, delete_treehole |
| 许愿池 | list_wishes, manage_wish |
| 网站配置 | get_site_config, update_site_config |
| 分类 | list_categories |
| 简历·工作 | create_work_experience, update_work_experience, delete_work_experience, list_work_experiences |
| 简历·项目 | create_project, update_project, delete_project, list_projects |
| 资源 & 统计 | upload_image, get_dashboard_stats |

All tools execute via `LuneApiClient` → HTTP calls to `lune-web` admin API.

## Routing

领域路由（零 LLM 调用）由 `AgentProfiles` 注册表统一声明：`@文章/@随笔/...` 显式前缀 +
关键词命中。恰好命中一个领域 → 该领域；零个/多个 → 通用兜底。新增领域只需在
`AgentProfiles` 加一条常量，无需改动 `AgentOrchestrator`。

## Configuration

### Profiles

| Profile | Redis Host | Lune API | Log Level |
|---------|-----------|----------|-----------|
| `local` | localhost | http://localhost:8081 | DEBUG |
| `dev` | redis (Docker) | http://backend:8081 | DEBUG |
| `prod` | redis (Docker) | http://backend:8081 | WARN |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `AGENT_PORT` | 8082 | Server port |
| `AGENT_API_KEY` | - | DeepSeek API key (required) |
| `AGENT_BASE_URL` | https://aigw.phigent.cn | API gateway |
| `AGENT_MODEL` | deepseek/deepseek-v4-flash | Model name |
| `REDIS_HOST` | localhost | Redis host |
| `LUNE_API_URL` | http://localhost:8081 | lune-web base URL |

### Local Development

```bash
cp src/main/resources/application-local.yml.template \
   src/main/resources/application-local.yml
# Edit application-local.yml with your API key
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

> **⚠️ 容器启动必须带 `--env-file .env.local`**
> 推荐用 `make dev`（内部已带 `--env-file .env.local`）。
> 若直接 `docker compose up`，`JWT_SECRET` 会回退到默认开发密钥、`AGENT_API_KEY` 为空，
> 导致所有 `/api/admin/agent/**` 返回 403、聊天提示「请先配置 API Key」。
> 启动后若出现 403，优先检查 JWT_SECRET 与后端是否一致（长度/指纹）。

## Build & Deploy

```bash
# Build JAR
mvn clean package -DskipTests

# Run as host process (Docker Hub unreachable)
java -jar target/lune-agent.jar --spring.profiles.active=local

# Docker (requires Docker Hub access)
docker build -t lune-agent:latest .
docker compose up -d agent
```

## Dependencies

- Spring Boot 3.3.5 (Web, Data Redis, Actuator)
- Hutool 5.8.29 (JSON, utilities)
- Java 17+
- Redis (for chat memory)
