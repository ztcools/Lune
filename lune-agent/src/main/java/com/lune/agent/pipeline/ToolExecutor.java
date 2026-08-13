package com.lune.agent.pipeline;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.client.LuneApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 工具执行器 —— 注册表模式。
 *
 * <p>每种工具按领域分组为内部 Handler 类，通过 {@code Map<String, ToolHandler>} 路由。
 * 新增工具只需在对应 Handler 的 switch 中添加 case，无需修改本类的 dispatch 逻辑。
 * 工具超时由 {@link LuneApiClient} 的 HTTP 客户端（30s）兜底。</p>
 */
@Component
public class ToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);

    private final Map<String, ToolHandler> handlers = new LinkedHashMap<>();

    public ToolExecutor(LuneApiClient api) {
        var articleHandler = new ArticleHandler(api);
        var essayHandler = new EssayHandler(api);
        var recordHandler = new RecordHandler(api);
        var treeHoleHandler = new TreeHoleHandler(api);
        var wishHandler = new WishHandler(api);
        var configHandler = new ConfigHandler(api);
        var categoryHandler = new CategoryHandler(api);
        var resumeHandler = new ResumeHandler(api);
        var resourceHandler = new ResourceHandler(api);
        var dashboardHandler = new DashboardHandler(api);

        register(articleHandler, "create_article", "publish_article", "update_article", "delete_article", "list_articles");
        register(essayHandler, "create_essay", "delete_essay", "list_essays");
        register(recordHandler, "create_record", "delete_record", "list_records");
        register(treeHoleHandler, "list_treeholes", "delete_treehole");
        register(wishHandler, "list_wishes", "manage_wish");
        register(configHandler, "get_site_config", "update_site_config");
        register(categoryHandler, "list_categories");
        register(resumeHandler, "create_work_experience", "update_work_experience",
                "delete_work_experience", "list_work_experiences",
                "create_project", "update_project", "delete_project", "list_projects");
        register(resourceHandler, "upload_image");
        register(dashboardHandler, "get_dashboard_stats");
    }

    private void register(ToolHandler handler, String... names) {
        for (var name : names) handlers.put(name, handler);
    }

    /**
     * 执行工具调用。超时由 {@link LuneApiClient} 的 HTTP 客户端（30s）兜底，
     * 此处直接同步执行，避免嵌套线程池。
     */
    public Map<String, Object> execute(String name, Map<String, Object> args, String token) {
        var handler = handlers.get(name);
        if (handler == null) return m("success", false, "message", "未知工具: " + name);
        try {
            return handler.execute(name, args, token);
        } catch (Exception e) {
            log.error("Tool {} error: {}", name, e.getMessage(), e);
            return m("success", false, "message", "执行失败: " + e.getMessage());
        }
    }

    // ── ToolHandler ──

    @FunctionalInterface
    public interface ToolHandler {
        Map<String, Object> execute(String name, Map<String, Object> args, String token);
    }

    // ── Shared helpers ──

    static Map<String, Object> m(Object... kv) {
        var r = new HashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    static long num(Map<String, Object> m, String k) {
        var v = m.get(k);
        return v instanceof Number n ? n.longValue() : 0L;
    }

    static JSONObject mapObj(Map<String, Object> src, String... keys) {
        var dst = new JSONObject();
        for (var k : keys) if (src.containsKey(k) && src.get(k) != null) dst.set(k, src.get(k));
        return dst;
    }

    static void copy(Map<String, Object> src, JSONObject dst, String... keys) {
        for (var k : keys) if (src.containsKey(k) && src.get(k) != null) dst.set(k, src.get(k));
    }

    static Map<String, Object> preview(JSONObject obj) {
        var m = new LinkedHashMap<String, Object>();
        for (var k : obj.keySet()) m.put(k, obj.get(k));
        return m;
    }

    /** 删除类工具的统一返回：根据 API 删除结果上报成败。 */
    static Map<String, Object> deleted(boolean ok) {
        return m("success", ok, "message", ok ? "已删除" : "删除失败");
    }

    // ──── Domain Handlers ────

    static class ArticleHandler implements ToolHandler {
        private final LuneApiClient api;
        ArticleHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "create_article" -> {
                    var body = new JSONObject();
                    body.set("title", a.get("title"));
                    body.set("content", a.get("content"));
                    body.set("summary", a.getOrDefault("summary", ""));
                    body.set("categoryId", a.getOrDefault("categoryId", null));
                    body.set("cover", a.getOrDefault("cover", null));
                    body.set("status", 0); // 直接创建为草稿，避免二次写库
                    var opt = api.createArticle(body, token);
                    if (opt.isEmpty()) yield m("success", false, "message", "创建失败");
                    var obj = opt.get();
                    yield m("success", true, "message", "文章草稿已创建，说「发」即发布", "preview", preview(obj));
                }
                case "publish_article" -> {
                    long id = num(a, "id");
                    var title = a.get("title");
                    var content = a.get("content");
                    // 后端 updateArticle 会整字段覆盖，缺 title/content 时用占位符会清空正文，故缺则失败
                    if (title == null || String.valueOf(title).isBlank()
                            || content == null || String.valueOf(content).isBlank()) {
                        yield m("success", false, "message", "缺少标题或正文，请先查询文章内容后再发布");
                    }
                    var body = new JSONObject();
                    body.set("title", title);
                    body.set("content", content);
                    body.set("status", 1);
                    if (api.updateArticle(id, body, token).isPresent())
                        yield m("success", true, "message", "文章 ID=" + id + " 已发布", "articleId", id);
                    yield m("success", false, "message", "发布失败");
                }
                case "update_article" -> {
                    long id = num(a, "id");
                    var body = new JSONObject();
                    copy(a, body, "title", "content", "summary", "categoryId", "cover");
                    var opt = api.updateArticle(id, body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "已更新", "preview", preview(opt.get()));
                    yield m("success", false, "message", "更新失败");
                }
                case "delete_article" -> deleted(api.deleteArticle(num(a, "id"), token));
                case "list_articles" -> {
                    int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
                    int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
                    var opt = api.listArticles(page, size, token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    var obj = opt.get();
                    yield m("success", true, "message", "共 " + obj.get("total") + " 篇", "total", obj.get("total"), "articles", obj.get("records"));
                }
                default -> m("success", false, "message", "Unknown article tool: " + name);
            };
        }
    }

    static class EssayHandler implements ToolHandler {
        private final LuneApiClient api;
        EssayHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "create_essay" -> {
                    var body = mapObj(a, "content", "mood", "weather", "location", "media");
                    if (a.containsKey("media") && a.get("media") instanceof String s) {
                        try {
                            var arr = JSONUtil.parseArray(s);
                            var normalized = new cn.hutool.json.JSONArray();
                            for (int i = 0; i < arr.size(); i++) {
                                var item = arr.get(i);
                                if (item instanceof String url)
                                    normalized.add(new JSONObject().set("type", "image").set("url", url));
                                else if (item instanceof JSONObject obj) {
                                    if (!obj.containsKey("type")) obj.set("type", "image");
                                    normalized.add(obj);
                                }
                            }
                            body.set("media", normalized.toString());
                        } catch (Exception e) { /* keep as-is */ }
                    }
                    body.set("status", 1);
                    var opt = api.createEssay(body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "随笔已创建", "preview", preview(opt.get()));
                    yield m("success", false, "message", "创建失败");
                }
                case "delete_essay" -> deleted(api.deleteEssay(num(a, "id"), token));
                case "list_essays" -> {
                    int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
                    int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
                    var opt = api.listEssays(page, size, token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    var obj = opt.get();
                    yield m("success", true, "message", "共 " + obj.get("total") + " 条", "total", obj.get("total"), "essays", obj.get("records"));
                }
                default -> m("success", false, "message", "Unknown essay tool: " + name);
            };
        }
    }

    static class RecordHandler implements ToolHandler {
        private final LuneApiClient api;
        RecordHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "create_record" -> {
                    var body = mapObj(a, "content", "categoryId", "cover", "media");
                    // title 为 DB 必填列：优先用模型给的 title，否则用正文前 20 字兜底
                    var title = a.get("title");
                    if (title == null || String.valueOf(title).isBlank()) {
                        var content = (String) a.get("content");
                        var fallback = content != null && !content.isBlank()
                                ? content.replaceAll("\\s+", " ").trim() : "";
                        title = fallback.length() > 20 ? fallback.substring(0, 20)
                                : (fallback.isEmpty() ? "未命名记录" : fallback);
                    }
                    body.set("title", title);
                    var opt = api.createRecord(body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "记录已创建", "preview", preview(opt.get()));
                    yield m("success", false, "message", "创建失败");
                }
                case "delete_record" -> deleted(api.deleteRecord(num(a, "id"), token));
                case "list_records" -> {
                    int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
                    int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
                    Long cid = a.containsKey("categoryId") ? num(a, "categoryId") : null;
                    var opt = api.listRecords(page, size, cid, token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    var obj = opt.get();
                    yield m("success", true, "message", "共 " + obj.get("total") + " 条", "total", obj.get("total"), "records", obj.get("records"));
                }
                default -> m("success", false, "message", "Unknown record tool: " + name);
            };
        }
    }

    static class TreeHoleHandler implements ToolHandler {
        private final LuneApiClient api;
        TreeHoleHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "list_treeholes" -> {
                    int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
                    int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
                    var opt = api.listTreeHoles(page, size, token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    var obj = opt.get();
                    yield m("success", true, "message", "共 " + obj.get("total") + " 条", "total", obj.get("total"), "treeholes", obj.get("records"));
                }
                case "delete_treehole" -> deleted(api.deleteTreeHole(num(a, "id"), token));
                default -> m("success", false, "message", "Unknown treehole tool: " + name);
            };
        }
    }

    static class WishHandler implements ToolHandler {
        private final LuneApiClient api;
        WishHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "list_wishes" -> {
                    int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
                    int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
                    var opt = api.listWishes(page, size, token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    var obj = opt.get();
                    yield m("success", true, "message", "共 " + obj.get("total") + " 条", "total", obj.get("total"), "wishes", obj.get("records"));
                }
                case "manage_wish" -> {
                    if ("delete".equals(a.get("action"))) yield deleted(api.deleteWish(num(a, "id"), token));
                    yield m("success", false, "message", "未知操作");
                }
                default -> m("success", false, "message", "Unknown wish tool: " + name);
            };
        }
    }

    static class ConfigHandler implements ToolHandler {
        private final LuneApiClient api;
        ConfigHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "get_site_config" -> {
                    var opt = api.listSiteConfigs(token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    yield m("success", true, "message", "配置已获取", "configs", opt.get().get("records"));
                }
                case "update_site_config" -> {
                    var body = new JSONObject();
                    body.set("configKey", a.get("configKey"));
                    body.set("configValue", a.get("configValue"));
                    if (api.saveSiteConfig(body, token).isPresent())
                        yield m("success", true, "message", "配置 " + a.get("configKey") + " 已更新");
                    yield m("success", false, "message", "配置更新失败");
                }
                default -> m("success", false, "message", "Unknown config tool: " + name);
            };
        }
    }

    static class CategoryHandler implements ToolHandler {
        private final LuneApiClient api;
        CategoryHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            var opt = api.listCategories(token);
            if (opt.isEmpty()) return m("success", false, "message", "查询失败");
            return m("success", true, "message", "分类列表", "categories", opt.get().get("records"));
        }
    }

    static class ResumeHandler implements ToolHandler {
        private final LuneApiClient api;
        ResumeHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return switch (name) {
                case "create_work_experience" -> {
                    var body = mapObj(a, "company", "position", "location", "description", "startDate", "endDate");
                    body.set("isCurrent", a.getOrDefault("isCurrent", false));
                    body.set("status", 1);
                    var opt = api.createWorkExperience(body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "已创建", "preview", preview(opt.get()));
                    yield m("success", false, "message", "创建失败");
                }
                case "update_work_experience" -> {
                    long id = num(a, "id");
                    var body = mapObj(a, "company", "position", "location", "description", "startDate", "endDate");
                    body.set("isCurrent", a.getOrDefault("isCurrent", false));
                    var opt = api.updateWorkExperience(id, body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "已更新", "preview", preview(opt.get()));
                    yield m("success", false, "message", "更新失败");
                }
                case "delete_work_experience" -> deleted(api.deleteWorkExperience(num(a, "id"), token));
                case "list_work_experiences" -> {
                    var opt = api.listWorkExperiences(token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    yield m("success", true, "message", "检索成功", "experiences", opt.get().get("records"));
                }
                case "create_project" -> {
                    var body = mapObj(a, "name", "summary", "description", "role", "projectUrl");
                    body.set("status", 1);
                    if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
                    var opt = api.createProject(body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "已创建", "preview", preview(opt.get()));
                    yield m("success", false, "message", "创建失败");
                }
                case "update_project" -> {
                    long id = num(a, "id");
                    var body = mapObj(a, "name", "summary", "description", "role", "projectUrl");
                    if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
                    var opt = api.updateProject(id, body, token);
                    if (opt.isPresent()) yield m("success", true, "message", "已更新", "preview", preview(opt.get()));
                    yield m("success", false, "message", "更新失败");
                }
                case "delete_project" -> deleted(api.deleteProject(num(a, "id"), token));
                case "list_projects" -> {
                    var opt = api.listProjects(token);
                    if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                    yield m("success", true, "message", "检索成功", "projects", opt.get().get("records"));
                }
                default -> m("success", false, "message", "Unknown resume tool: " + name);
            };
        }
    }

    static class ResourceHandler implements ToolHandler {
        private final LuneApiClient api;
        ResourceHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            String url = (String) a.get("url");
            if (url == null || url.isBlank()) return m("success", false, "message", "请提供图片URL");
            String uploadPath = extractUploadPath(url);
            if (uploadPath != null) {
                var p = new JSONObject();
                p.set("path", uploadPath);
                return m("success", true, "message", "图片已就绪", "url", uploadPath, "preview", p);
            }
            if (!isSafeImageUrl(url)) return m("success", false, "message", "图片URL不合法（仅支持公网 http/https，禁止内网地址）");
            var opt = api.uploadFromUrl(url, token);
            if (opt.isPresent()) return m("success", true, "message", "图片已上传", "url", opt.get().get("path"), "preview", preview(opt.get()));
            return m("success", false, "message", "上传失败");
        }

        /** SSRF 防护：仅允许公网 http/https，拒绝 file://、内网/环回/链路本地/云元数据地址。 */
        private static boolean isSafeImageUrl(String url) {
            try {
                var u = java.net.URI.create(url);
                var scheme = u.getScheme();
                if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) return false;
                var host = u.getHost();
                if (host == null || host.isBlank()) return false;
                host = host.toLowerCase();
                if (host.equals("localhost") || host.endsWith(".localhost")
                        || host.equals("169.254.169.254") || host.equals("metadata.google.internal")) return false;
                if (host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
                    var ip = java.net.InetAddress.getByName(host);
                    if (ip.isLoopbackAddress() || ip.isLinkLocalAddress() || ip.isSiteLocalAddress()
                            || ip.isAnyLocalAddress() || ip.isMulticastAddress()) return false;
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private static String extractUploadPath(String url) {
            int idx = url.indexOf("/upload/");
            if (idx >= 0) {
                String path = url.substring(idx);
                int end = path.length();
                for (int i = 0; i < path.length(); i++) {
                    char c = path.charAt(i);
                    if (c == ' ' || c == '\n' || c == '\r' || c == ',' || c == ']' || c == '}') { end = i; break; }
                }
                return path.substring(0, end);
            }
            return null;
        }
    }

    static class DashboardHandler implements ToolHandler {
        private final LuneApiClient api;
        DashboardHandler(LuneApiClient api) { this.api = api; }
        public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
            return m("success", true, "message", "网站统计",
                    "articleCount", count(() -> api.listArticles(1, 1, token)),
                    "essayCount", count(() -> api.listEssays(1, 1, token)),
                    "treeholeCount", count(() -> api.listTreeHoles(1, 1, token)),
                    "wishCount", count(() -> api.listWishes(1, 1, token)));
        }
        private Object count(java.util.function.Supplier<Optional<JSONObject>> s) {
            return s.get().map(o -> o.get("total")).orElse(0);
        }
    }
}
