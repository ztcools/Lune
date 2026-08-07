package com.lune.agent.pipeline;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.client.LuneApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);
    private final LuneApiClient api;

    public ToolExecutor(LuneApiClient api) { this.api = api; }

    /** @deprecated 使用 {@link ToolDefinitions#allDefinitions()}，保留以兼容 */
    public JSONArray getDefinitions() {
        return ToolDefinitions.allDefinitions();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(String name, Map<String, Object> args, String token) {
        try {
            Map<String, Object> result = switch (name) {
                case "create_article" -> createArticle(args, token);
                case "publish_article" -> publishArticle(args, token);
                case "update_article" -> updateArticle(args, token);
                case "delete_article" -> deleteArticle(args, token);
                case "list_articles" -> listArticles(args, token);
                case "create_essay" -> createEssay(args, token);
                case "delete_essay" -> deleteEssay(args, token);
                case "list_essays" -> listEssays(args, token);
                case "create_record" -> createRecord(args, token);
                case "delete_record" -> deleteRecord(args, token);
                case "list_records" -> listRecords(args, token);
                case "list_treeholes" -> listTreeHoles(args, token);
                case "delete_treehole" -> deleteTreeHole(args, token);
                case "list_wishes" -> listWishes(args, token);
                case "manage_wish" -> manageWish(args, token);
                case "get_site_config" -> getSiteConfig(args, token);
                case "update_site_config" -> updateSiteConfig(args, token);
                case "list_categories" -> listCategories(args, token);
                case "create_work_experience" -> createWorkExperience(args, token);
                case "update_work_experience" -> updateWorkExperience(args, token);
                case "delete_work_experience" -> deleteWorkExperience(args, token);
                case "list_work_experiences" -> listWorkExperiences(args, token);
                case "create_project" -> createProject(args, token);
                case "update_project" -> updateProject(args, token);
                case "delete_project" -> deleteProject(args, token);
                case "list_projects" -> listProjects(args, token);
                case "upload_image" -> uploadImage(args, token);
                case "get_dashboard_stats" -> getDashboardStats(args, token);
                default -> m("success", false, "message", "未知工具: " + name);
            };
            return result;
        } catch (Exception e) {
            log.error("Tool {} error: {}", name, e.getMessage());
            return m("success", false, "message", "执行失败: " + e.getMessage());
        }
    }

    // ── helpers ──

    private Map<String, Object> m(Object... kv) {
        var r = new HashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    private long num(Map<String, Object> m, String k) { return ((Number) m.get(k)).longValue(); }

    private JSONObject map(Map<String, Object> src, String... keys) {
        var dst = new JSONObject();
        for (var k : keys) if (src.containsKey(k) && src.get(k) != null) dst.set(k, src.get(k));
        return dst;
    }

    private void copy(Map<String, Object> src, JSONObject dst, String... keys) {
        for (var k : keys) if (src.containsKey(k) && src.get(k) != null) dst.set(k, src.get(k));
    }

    private Map<String, Object> preview(JSONObject obj) {
        var m = new LinkedHashMap<String, Object>();
        for (var k : obj.keySet()) m.put(k, obj.get(k));
        return m;
    }

    // ── Article ──

    private Map<String, Object> createArticle(Map<String, Object> a, String token) {
        var body = new JSONObject();
        body.set("title", a.get("title"));
        body.set("content", a.get("content"));
        body.set("summary", a.getOrDefault("summary", ""));
        body.set("categoryId", a.getOrDefault("categoryId", null));
        body.set("cover", a.getOrDefault("cover", null));
        var r = api.createArticle(body, token);
        if (r == null) return m("success", false, "message", "创建失败");
        // 后端 create 硬编码 status=1，需要更新为 0 设为草稿
        var draft = new JSONObject();
        draft.set("title", a.get("title"));
        draft.set("content", a.get("content"));
        draft.set("status", 0);
        api.updateArticle(r.getLong("id"), draft, token);
        return m("success", true, "message", "文章草稿已创建，说「发」即发布", "preview", preview(r));
    }

    private Map<String, Object> publishArticle(Map<String, Object> a, String token) {
        long id = num(a, "id");
        // Backend update requires title+content for @Valid
        var body = new JSONObject();
        body.set("title", a.getOrDefault("title", "article-" + id));
        body.set("content", a.getOrDefault("content", "<p></p>"));
        body.set("status", 1);
        var r = api.updateArticle(id, body, token);
        if (r != null) return m("success", true, "message", "文章 ID=" + id + " 已发布", "articleId", id);
        return m("success", false, "message", "发布失败");
    }

    private Map<String, Object> updateArticle(Map<String, Object> a, String token) {
        long id = num(a, "id");
        var body = new JSONObject();
        copy(a, body, "title", "content", "summary", "categoryId", "cover");
        var r = api.updateArticle(id, body, token);
        if (r != null) return m("success", true, "message", "已更新", "preview", preview(r));
        return m("success", false, "message", "更新失败");
    }

    private Map<String, Object> deleteArticle(Map<String, Object> a, String token) {
        api.deleteArticle(num(a, "id"), token);
        return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listArticles(Map<String, Object> a, String token) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listArticles(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 篇", "total", r.get("total"), "articles", r.get("records"));
    }

    // ── Essay ──

    private Map<String, Object> createEssay(Map<String, Object> a, String token) {
        var body = map(a, "content", "mood", "weather", "location", "media");
        // Normalize media: ensure [{type:"image",url:"..."}] format
        if (a.containsKey("media") && a.get("media") instanceof String s) {
            try {
                var arr = JSONUtil.parseArray(s);
                var normalized = new cn.hutool.json.JSONArray();
                for (int i = 0; i < arr.size(); i++) {
                    var item = arr.get(i);
                    if (item instanceof String url) {
                        normalized.add(new JSONObject().set("type", "image").set("url", url));
                    } else if (item instanceof JSONObject obj) {
                        if (!obj.containsKey("type")) obj.set("type", "image");
                        normalized.add(obj);
                    }
                }
                body.set("media", normalized.toString());
            } catch (Exception e) { /* keep as-is */ }
        }
        body.set("status", 1);
        var r = api.createEssay(body, token);
        if (r != null) return m("success", true, "message", "随笔已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> deleteEssay(Map<String, Object> a, String token) {
        api.deleteEssay(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listEssays(Map<String, Object> a, String token) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listEssays(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "essays", r.get("records"));
    }

    // ── Record ──

    private Map<String, Object> createRecord(Map<String, Object> a, String token) {
        var body = map(a, "content", "categoryId", "cover", "media");
        var r = api.createRecord(body, token);
        if (r != null) return m("success", true, "message", "记录已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> deleteRecord(Map<String, Object> a, String token) {
        api.deleteRecord(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listRecords(Map<String, Object> a, String token) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        Long cid = a.containsKey("categoryId") ? num(a, "categoryId") : null;
        var r = api.listRecords(page, size, cid, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "records", r.get("records"));
    }

    // ── TreeHole ──

    private Map<String, Object> listTreeHoles(Map<String, Object> a, String token) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listTreeHoles(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "treeholes", r.get("records"));
    }

    private Map<String, Object> deleteTreeHole(Map<String, Object> a, String token) {
        api.deleteTreeHole(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    // ── Wish ──

    private Map<String, Object> listWishes(Map<String, Object> a, String token) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listWishes(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "wishes", r.get("records"));
    }

    private Map<String, Object> manageWish(Map<String, Object> a, String token) {
        if ("delete".equals(a.get("action"))) { api.deleteWish(num(a, "id"), token); return m("success", true, "message", "已删除"); }
        return m("success", false, "message", "未知操作");
    }

    // ── Site Config ──

    private Map<String, Object> getSiteConfig(Map<String, Object> a, String token) {
        var r = api.listSiteConfigs(token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "配置已获取", "configs", r.get("records"));
    }

    private Map<String, Object> updateSiteConfig(Map<String, Object> a, String token) {
        var body = new JSONObject();
        body.set("configKey", a.get("configKey"));
        body.set("configValue", a.get("configValue"));
        api.saveSiteConfig(body, token);
        return m("success", true, "message", "配置 " + a.get("configKey") + " 已更新");
    }

    // ── Categories ──

    private Map<String, Object> listCategories(Map<String, Object> a, String token) {
        var r = api.listCategories(token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "分类列表", "categories", r.get("records"));
    }

    // ── Work Experience ──

    private Map<String, Object> createWorkExperience(Map<String, Object> a, String token) {
        var body = map(a, "company", "position", "location", "description", "startDate", "endDate");
        body.set("isCurrent", a.getOrDefault("isCurrent", false));
        body.set("status", 1);
        var r = api.createWorkExperience(body, token);
        if (r != null) return m("success", true, "message", "已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> updateWorkExperience(Map<String, Object> a, String token) {
        long id = num(a, "id");
        var body = map(a, "company", "position", "location", "description", "startDate", "endDate");
        body.set("isCurrent", a.getOrDefault("isCurrent", false));
        var r = api.updateWorkExperience(id, body, token);
        if (r != null) return m("success", true, "message", "已更新", "preview", preview(r));
        return m("success", false, "message", "更新失败");
    }

    private Map<String, Object> deleteWorkExperience(Map<String, Object> a, String token) {
        api.deleteWorkExperience(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listWorkExperiences(Map<String, Object> a, String token) {
        var r = api.listWorkExperiences(token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "检索成功", "experiences", r.get("records"));
    }

    // ── Project ──

    private Map<String, Object> createProject(Map<String, Object> a, String token) {
        var body = map(a, "name", "summary", "description", "role", "projectUrl");
        body.set("status", 1);
        if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
        var r = api.createProject(body, token);
        if (r != null) return m("success", true, "message", "已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> updateProject(Map<String, Object> a, String token) {
        long id = num(a, "id");
        var body = map(a, "name", "summary", "description", "role", "projectUrl");
        if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
        var r = api.updateProject(id, body, token);
        if (r != null) return m("success", true, "message", "已更新", "preview", preview(r));
        return m("success", false, "message", "更新失败");
    }

    private Map<String, Object> deleteProject(Map<String, Object> a, String token) {
        api.deleteProject(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listProjects(Map<String, Object> a, String token) {
        var r = api.listProjects(token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "检索成功", "projects", r.get("records"));
    }

    // ── Image Upload ──

    private Map<String, Object> uploadImage(Map<String, Object> a, String token) {
        String url = (String) a.get("url");
        if (url == null || url.isBlank()) return m("success", false, "message", "请提供图片URL");
        // 本地已上传的文件——提取 path 段判断，兼容本地 /upload/xxx 和生产 https://cdn/upload/xxx
        String uploadPath = extractUploadPath(url);
        if (uploadPath != null) {
            var preview = new JSONObject();
            preview.set("path", uploadPath);
            return m("success", true, "message", "图片已就绪", "url", uploadPath, "preview", preview);
        }
        // 外部URL → 下载后存入存储（本地磁盘或COS，由 STORAGE_TYPE 决定）
        var r = api.uploadFromUrl(url, token);
        if (r != null) return m("success", true, "message", "图片已上传", "url", r.get("path"), "preview", preview(r));
        return m("success", false, "message", "上传失败");
    }

    /** 从 URL 中提取 /upload/... 路径段，若非本站资源则返回 null */
    private static String extractUploadPath(String url) {
        int idx = url.indexOf("/upload/");
        if (idx >= 0) {
            String path = url.substring(idx);
            // 截断到下一个空格/换行/逗号（防止 LLM 拼接多余文本）
            int end = path.length();
            for (int i = 0; i < path.length(); i++) {
                char c = path.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == ',' || c == ']' || c == '}') {
                    end = i;
                    break;
                }
            }
            return path.substring(0, end);
        }
        return null;
    }

    // ── Dashboard ──

    private Map<String, Object> getDashboardStats(Map<String, Object> a, String token) {
        var r = new HashMap<String, Object>();
        r.put("success", true); r.put("message", "网站统计");
        var arts = api.listArticles(1, 1, token);
        r.put("articleCount", arts != null ? arts.get("total") : 0);
        var essays = api.listEssays(1, 1, token);
        r.put("essayCount", essays != null ? essays.get("total") : 0);
        var holes = api.listTreeHoles(1, 1, token);
        r.put("treeholeCount", holes != null ? holes.get("total") : 0);
        var wishes = api.listWishes(1, 1, token);
        r.put("wishCount", wishes != null ? wishes.get("total") : 0);
        return r;
    }
}
