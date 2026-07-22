# Lune - 记录美好生活

## Project Overview

Lune is a full-stack personal blog and lifestyle web application. It features articles, essays, love notes (Record), tree holes (anonymous messages), a treasure box (favorites & links), family/friends list, and a full admin dashboard.

- **Backend**: Spring Boot 3.3.5 + Java 17 + Maven
- **Frontend**: Vue 3 + Vite 5 + Pinia + Vue Router 4 + Element Plus
- **Database**: MySQL + Redis
- **Persistence**: MyBatis-Plus 3.5.7 (logic delete enabled)
- **Security**: Spring Security + JWT (jjwt 0.12.6) + BCrypt

---

## Project Structure

```
lune-server/                   # Maven parent POM (pom packaging)
├── pom.xml                    # Parent: Spring Boot 3.3.5, Java 17, dependency management
└── lune-web/                  # Spring Boot web module (the actual app)
    ├── pom.xml
    └── src/main/java/com/lune/
        ├── LuneApplication.java          # @SpringBootApplication + @MapperScan("com.lune.mapper")
        ├── common/
        │   ├── Result.java               # Unified response: { code, message, data }
        │   ├── PageResult.java            # Paginated response wrapper
        │   ├── BusinessException.java     # Custom business exception
        │   └── GlobalExceptionHandler.java
        ├── config/
        │   ├── SecurityConfig.java        # Spring Security: stateless, CORS, permitAll GET, /api/admin/** → ADMIN only
        │   ├── CorsConfig.java
        │   ├── MyBatisPlusConfig.java
        │   ├── RedisConfig.java
        │   ├── WebMvcConfig.java
        │   └── DataInitializer.java       # Seed data
        ├── security/
        │   ├── JwtTokenProvider.java      # Create/parse/validate JWT (userId, username, role claims)
        │   └── JwtAuthFilter.java         # OncePerRequestFilter: extracts Bearer token, sets SecurityContext
        ├── entity/                        # MyBatis-Plus entities (@TableName, @TableId, @TableLogic)
        │   ├── User.java                  # id, username, password, nickname, email, avatar, role, status
        │   ├── Article.java               # title, content, summary, cover, viewCount, likeCount, status, isTop
        │   ├── Category.java
        │   ├── Tag.java
        │   ├── Comment.java
        │   ├── Essay.java                 # Short essays/notes
        │   ├── Record.java                # Love notes / relationship records
        │   ├── TreeHole.java              # Anonymous messages
        │   ├── Family.java                # Friends/family members
        │   ├── Favorite.java              # Treasure box - favorites
        │   ├── FriendLink.java            # Treasure box - external links
        │   ├── SiteConfig.java            # Site settings (key-value)
        │   ├── Resource.java              # Uploaded file resources
        │   └── VisitLog.java
        ├── dto/                           # Request/Response DTOs
        │   ├── ArticleRequest.java
        │   ├── CommentRequest.java
        │   ├── LoginRequest.java / LoginResponse.java
        │   └── RegisterRequest.java
        ├── mapper/                        # MyBatis-Plus BaseMapper interfaces
        ├── service/                       # Service interfaces
        └── service/impl/                  # Service implementations
            ├── controller/                # Public REST controllers (/api/...)
            │   ├── ArticleController.java
            │   ├── AuthController.java
            │   ├── CategoryController.java
            │   ├── CommentController.java
            │   ├── EssayController.java
            │   ├── FamilyController.java
            │   ├── FriendLinkController.java
            │   ├── RecordController.java
            │   ├── SiteConfigController.java
            │   ├── TagController.java
            │   ├── TreasureController.java
            │   └── TreeHoleController.java
            └── controller/admin/          # Admin REST controllers (/api/admin/...)
                ├── AdminArticleController.java
                ├── AdminCategoryController.java
                ├── AdminCommentController.java
                ├── AdminEssayController.java
                ├── AdminFamilyController.java
                ├── AdminRecordController.java
                ├── AdminResourceController.java
                ├── AdminSiteConfigController.java
                ├── AdminTagController.java
                ├── AdminTreasureController.java
                ├── AdminTreeHoleController.java
                └── AdminUserController.java

lune-ui/                                 # Vue 3 frontend
├── index.html                           # zh-CN, title: "Lune - 记录美好生活"
├── vite.config.js                       # Port 5173, proxy /api and /upload → localhost:8081
├── package.json                         # vue 3.4, vue-router 4, pinia 2, element-plus 2, axios 1
└── src/
    ├── main.js                          # createApp, use Pinia/Router/ElementPlus/Icons, mount #app
    ├── App.vue                          # Just <router-view />
    ├── api/
    │   ├── request.js                   # Axios instance: baseURL=/api, Bearer token interceptor, response unwraps data.data
    │   └── modules.js                   # All API functions (authApi, articleApi, categoryApi, tagApi, ...)
    ├── router/index.js                  # Routes: Landing → PublicLayout(children) + AdminLogin + AdminLayout(children, requiresAuth)
    ├── stores/
    │   ├── user.js                      # Pinia store: token, user, login/logout actions
    │   └── app.js
    ├── layout/
    │   ├── PublicLayout.vue             # Public page layout with header/footer
    │   └── AdminLayout.vue              # Admin dashboard layout with sidebar
    ├── views/                           # Public-facing pages
    │   ├── landing/Landing.vue          # Landing/splash page (route: /)
    │   ├── home/Home.vue                # Main blog home (route: /home)
    │   ├── article/ArticleDetail.vue    # Article detail (route: /article/:id)
    │   ├── family/Family.vue            # Friends & family page
    │   ├── treehole/TreeHole.vue        # Anonymous tree hole messages
    │   ├── essay/Essay.vue              # Essays page
    │   ├── record/Record.vue            # Love notes page
    │   └── treasure/Treasure.vue        # Treasure box page
    ├── admin/                           # Admin management pages
    │   ├── Login.vue                    # Admin login page (/admin/login)
    │   ├── Dashboard.vue                # Dashboard with stats
    │   ├── ArticleManage.vue            # Article CRUD
    │   ├── CategoryManage.vue
    │   ├── CommentManage.vue
    │   ├── EssayManage.vue
    │   ├── RecordManage.vue
    │   ├── TreeHoleManage.vue
    │   ├── TreasureManage.vue
    │   ├── FamilyManage.vue
    │   ├── UserManage.vue
    │   ├── ResourceManage.vue           # File upload management
    │   └── Settings.vue                 # Site configuration
    └── assets/styles/
        ├── variables.css
        ├── global.css
        └── animations.css
```

---

## Architecture Patterns

### Backend (Spring Boot)

**Layered architecture**: Controller → Service → Mapper → Entity

**Response format** — All endpoints return `Result<T>`:
```json
{ "code": 200, "message": "success", "data": { ... } }
```
- Success: `Result.success(data)` — code 200
- Failure: `Result.fail("message")` — code 500
- Unauthorized: `Result.unauthorized()` — code 401
- Forbidden: `Result.forbidden()` — code 403

**Pagination** — Public list endpoints use `PageResult<T>`:
```java
PageResult.of(records, total, page, size)
```

**Service pattern** every module follows:
- Interface in `service/XxxService.java`
- Implementation in `service/impl/XxxServiceImpl.java` — constructor injection, `@Service`
- Mapper: `XxxMapper extends BaseMapper<Xxx>` (empty interface, MyBatis-Plus handles CRUD)
- Queries use `LambdaQueryWrapper<Xxx>` for type-safe conditions

**Dual controller pattern** for most entities:
- Public controller at `/api/{resource}` — read-only GET endpoints, no auth required
- Admin controller at `/api/admin/{resource}` — full CRUD, annotated `@PreAuthorize("hasRole('ADMIN')")`

**Security flow**:
1. `/api/auth/login` → validates credentials (BCrypt) → returns JWT token
2. All subsequent requests: `Authorization: Bearer <token>` header
3. `JwtAuthFilter` extracts token, validates, sets `SecurityContext` with userId/username/role
4. `/api/admin/**` requires ADMIN role; GET on public `/api/**` is permitAll
5. Stateless sessions (`SessionCreationPolicy.STATELESS`), CSRF disabled

**Logic delete**: All entities use MyBatis-Plus `@TableLogic` — `deleted=0` (not deleted), `deleted=1` (deleted). `deleteById()` performs soft delete automatically.

**Database initialization**: `DataInitializer` creates default admin user (`admin`/`admin123`) and sample categories on first startup.

### Frontend (Vue 3)

**Request flow** — `request.js` wraps axios:
1. `baseURL: '/api'` — proxied by Vite to `localhost:8081`
2. Request interceptor: attaches `Authorization: Bearer <token>` from localStorage
3. Response interceptor: unwraps response → if `data.code === 200`, returns `data.data` directly; on 401/403, clears auth and redirects to login

**All API functions in `modules.js`** are exported as named objects grouped by domain:
```js
export const articleApi = { list, getById, create, update, delete }
export const authApi = { login, register, logout }
// ... etc.
```
Callers use them as: `await articleApi.list({ page: 1, size: 10, categoryId: 5 })`

**Routing**: Two layout trees
- `PublicLayout` wraps: Home, ArticleDetail, Family, TreeHole, Essay, Record, Treasure
- `AdminLayout` wraps: Dashboard, CRUD management pages (requires auth via `router.beforeEach` + token check)

**State management** — Pinia stores:
- `user.js`: token, user object, `login()`/`logout()` actions, computed getters (isAdmin, isLoggedIn, nickname)
- `app.js`: app-level state

---

## Configuration

### application.yml key settings
| Setting | Value |
|---------|-------|
| Server port | `8081` |
| DB URL | `jdbc:mysql://localhost:3306/lune` |
| DB credentials | `${DB_USERNAME:root}` / `${DB_PASSWORD:zt921921}` |
| Redis | `${REDIS_HOST:localhost}:${REDIS_PORT:6379}` |
| JWT secret | `${JWT_SECRET:lune-jwt-secret-key-change-in-production-min-256bits}` |
| JWT expiration | `86400000` (24 hours) |
| Upload path | `${UPLOAD_PATH:./upload}` |
| Max upload size | `100MB` |

### Vite proxy
| Frontend path | Proxied to |
|--------------|------------|
| `/api` | `http://localhost:8081` |
| `/upload` | `http://localhost:8081` |

---

## How to Run

### Backend
```bash
cd lune-server/lune-web
# Requires: Java 17, MySQL (database: lune), Redis
# Default admin: admin / admin123
mvn spring-boot:run
# Runs on http://localhost:8081
```

### Frontend
```bash
cd lune-ui
npm install
npm run dev
# Runs on http://localhost:5173
```

### Database
SQL schema is at `lune-server/lune-web/src/main/resources/sql/lune.sql`.

---

## Key Conventions

- **Use constructor injection** (not `@Autowired`) — all service and controller classes use `private final` fields with constructor injection
- **Use `var` for local variables** in service implementations (Java 17)
- **Use Lombok `@Data`** on all entities
- **MyBatis-Plus `LambdaQueryWrapper`** for all queries — type-safe, no magic strings
- **No XML mappers** needed — all queries are LambdaQueryWrapper-based; `mapper-locations` is configured but currently no XML files exist
- **Admin UIs follow a pattern**: Each `XxxManage.vue` has a table listing + dialog for create/edit, calls the corresponding API module
- **Static assets** are in `lune-ui/public/assets/` (fonts, logos, background images) and `lune-ui/public/upload/` (user-uploaded images)
- **Frontend styles**: Three CSS files in `assets/styles/` — `variables.css` (CSS custom properties), `global.css` (base styles), `animations.css`

## Business Domains

| Module | Path prefix | Description |
|--------|-----------|-------------|
| Auth | `/api/auth` | Login, register, logout |
| Article | `/api/articles` | Blog articles with categories, tags |
| Category | `/api/categories` | Article categories, filterable by type |
| Comment | `/api/comments` | Article comments |
| Essay | `/api/essays` | Short essays/notes |
| Record | `/api/records` | Love notes / couple memories |
| TreeHole | `/api/treeholes` | Anonymous message board |
| Treasure | `/api/treasures` | Curated favorites + external links |
| Family | `/api/family` | Friends/family member profiles |
| FriendLink | `/api/friend-links` | Friend links (public read-only) |
| SiteConfig | `/api/site-config` | Key-value site settings |
| Resource | `/api/admin/resources` | File upload/management (admin only) |
| User | `/api/admin/users` | User management (admin only) |
