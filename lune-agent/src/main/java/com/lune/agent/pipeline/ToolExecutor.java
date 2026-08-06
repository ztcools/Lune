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

    private String token;
    public void setToken(String token) { this.token = token; }

    public JSONArray getDefinitions() {
        return JSONUtil.parseArray("""
        [{"type":"function","function":{"name":"create_article","description":"创建文章草稿。需要标题+内容，可选摘要/分类/封面。建完告诉用户id。","parameters":{"type":"object","properties":{"title":{"type":"string"},"content":{"type":"string","description":"HTML格式"},"summary":{"type":"string"},"categoryId":{"type":"integer"},"cover":{"type":"string"}},"required":["title","content"]}}},
        {"type":"function","function":{"name":"publish_article","description":"发布指定id的文章。必须传入title+content（从create_article结果中获取）。","parameters":{"type":"object","properties":{"id":{"type":"integer"},"title":{"type":"string"},"content":{"type":"string"}},"required":["id","title","content"]}}},
        {"type":"function","function":{"name":"update_article","description":"更新文章","parameters":{"type":"object","properties":{"id":{"type":"integer"},"title":{"type":"string"},"content":{"type":"string"},"summary":{"type":"string"},"categoryId":{"type":"integer"},"cover":{"type":"string"}},"required":["id"]}}},
        {"type":"function","function":{"name":"delete_article","description":"删除文章，先确认","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}},
        {"type":"function","function":{"name":"list_articles","description":"查文章列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}},
        {"type":"function","function":{"name":"create_essay","description":"创建随笔","parameters":{"type":"object","properties":{"title":{"type":"string"},"content":{"type":"string"},"mood":{"type":"string"},"weather":{"type":"string"},"location":{"type":"string"}},"required":["title","content"]}}},
        {"type":"function","function":{"name":"delete_essay","description":"删除随笔","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}},
        {"type":"function","function":{"name":"list_essays","description":"查随笔列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}},
        {"type":"function","function":{"name":"create_record","description":"创建记录，必须给categoryId","parameters":{"type":"object","properties":{"title":{"type":"string"},"content":{"type":"string"},"categoryId":{"type":"integer"}},"required":["title","content","categoryId"]}}},
        {"type":"function","function":{"name":"delete_record","description":"删除记录","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}},
        {"type":"function","function":{"name":"list_records","description":"查记录列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"},"categoryId":{"type":"integer"}}}}},
        {"type":"function","function":{"name":"list_treeholes","description":"查树洞列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}},
        {"type":"function","function":{"name":"delete_treehole","description":"删树洞消息","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}},
        {"type":"function","function":{"name":"list_wishes","description":"查许愿池","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}},
        {"type":"function","function":{"name":"manage_wish","description":"管理许愿(删除)","parameters":{"type":"object","properties":{"id":{"type":"integer"},"action":{"type":"string","enum":["delete"]}},"required":["id","action"]}}},
        {"type":"function","function":{"name":"get_site_config","description":"获取所有网站配置","parameters":{"type":"object","properties":{}}}},
        {"type":"function","function":{"name":"update_site_config","description":"改网站配置(key如site_name/site_logo/beian_icp等)","parameters":{"type":"object","properties":{"configKey":{"type":"string"},"configValue":{"type":"string"}},"required":["configKey","configValue"]}}},
        {"type":"function","function":{"name":"list_categories","description":"列出文章分类","parameters":{"type":"object","properties":{}}}},
        {"type":"function","function":{"name":"create_work_experience","description":"创建简历工作经历","parameters":{"type":"object","properties":{"company":{"type":"string"},"position":{"type":"string"},"startDate":{"type":"string"},"endDate":{"type":"string"},"isCurrent":{"type":"boolean"},"description":{"type":"string"}},"required":["company","position","startDate"]}}},
        {"type":"function","function":{"name":"delete_work_experience","description":"删工作经历","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}},
        {"type":"function","function":{"name":"create_project","description":"创建简历项目","parameters":{"type":"object","properties":{"name":{"type":"string"},"summary":{"type":"string"},"description":{"type":"string"},"techStack":{"type":"array","items":{"type":"string"}},"role":{"type":"string"},"projectUrl":{"type":"string"}},"required":["name","summary"]}}},
        {"type":"function","function":{"name":"delete_project","description":"删项目","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}},
        {"type":"function","function":{"name":"get_dashboard_stats","description":"网站统计数据(文章/随笔/记录/树洞/许愿总数)","parameters":{"type":"object","properties":{}}}}]}""");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(String name, Map<String, Object> args) {
        try {
            Map<String, Object> result = switch (name) {
                case "create_article" -> createArticle(args);
                case "publish_article" -> publishArticle(args);
                case "update_article" -> updateArticle(args);
                case "delete_article" -> deleteArticle(args);
                case "list_articles" -> listArticles(args);
                case "create_essay" -> createEssay(args);
                case "delete_essay" -> deleteEssay(args);
                case "list_essays" -> listEssays(args);
                case "create_record" -> createRecord(args);
                case "delete_record" -> deleteRecord(args);
                case "list_records" -> listRecords(args);
                case "list_treeholes" -> listTreeHoles(args);
                case "delete_treehole" -> deleteTreeHole(args);
                case "list_wishes" -> listWishes(args);
                case "manage_wish" -> manageWish(args);
                case "get_site_config" -> getSiteConfig(args);
                case "update_site_config" -> updateSiteConfig(args);
                case "list_categories" -> listCategories(args);
                case "create_work_experience" -> createWorkExperience(args);
                case "delete_work_experience" -> deleteWorkExperience(args);
                case "create_project" -> createProject(args);
                case "delete_project" -> deleteProject(args);
                case "get_dashboard_stats" -> getDashboardStats(args);
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

    private Map<String, Object> createArticle(Map<String, Object> a) {
        var body = new JSONObject();
        body.set("title", a.get("title"));
        body.set("content", a.get("content"));
        body.set("summary", a.getOrDefault("summary", ""));
        body.set("categoryId", a.getOrDefault("categoryId", null));
        body.set("cover", a.getOrDefault("cover", null));
        var r = api.createArticle(body, token);
        if (r == null) return m("success", false, "message", "创建失败");
        // Set draft
        api.updateArticle(r.getLong("id"), new JSONObject().set("status", 0), token);
        return m("success", true, "message", "文章草稿已创建", "preview", preview(r));
    }

    private Map<String, Object> publishArticle(Map<String, Object> a) {
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

    private Map<String, Object> updateArticle(Map<String, Object> a) {
        long id = num(a, "id");
        var body = new JSONObject();
        copy(a, body, "title", "content", "summary", "categoryId", "cover");
        var r = api.updateArticle(id, body, token);
        if (r != null) return m("success", true, "message", "已更新", "preview", preview(r));
        return m("success", false, "message", "更新失败");
    }

    private Map<String, Object> deleteArticle(Map<String, Object> a) {
        api.deleteArticle(num(a, "id"), token);
        return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listArticles(Map<String, Object> a) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listArticles(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 篇", "total", r.get("total"), "articles", r.get("records"));
    }

    // ── Essay ──

    private Map<String, Object> createEssay(Map<String, Object> a) {
        var body = map(a, "title", "content", "mood", "weather", "location");
        body.set("status", 1);
        var r = api.createEssay(body, token);
        if (r != null) return m("success", true, "message", "随笔已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> deleteEssay(Map<String, Object> a) {
        api.deleteEssay(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listEssays(Map<String, Object> a) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listEssays(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "essays", r.get("records"));
    }

    // ── Record ──

    private Map<String, Object> createRecord(Map<String, Object> a) {
        var body = map(a, "title", "content", "categoryId");
        var r = api.createRecord(body, token);
        if (r != null) return m("success", true, "message", "记录已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> deleteRecord(Map<String, Object> a) {
        api.deleteRecord(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    private Map<String, Object> listRecords(Map<String, Object> a) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        Long cid = a.containsKey("categoryId") ? num(a, "categoryId") : null;
        var r = api.listRecords(page, size, cid, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "records", r.get("records"));
    }

    // ── TreeHole ──

    private Map<String, Object> listTreeHoles(Map<String, Object> a) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listTreeHoles(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "treeholes", r.get("records"));
    }

    private Map<String, Object> deleteTreeHole(Map<String, Object> a) {
        api.deleteTreeHole(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    // ── Wish ──

    private Map<String, Object> listWishes(Map<String, Object> a) {
        int page = a.containsKey("page") ? ((Number) a.get("page")).intValue() : 1;
        int size = a.containsKey("size") ? ((Number) a.get("size")).intValue() : 10;
        var r = api.listWishes(page, size, token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "共 " + r.get("total") + " 条", "total", r.get("total"), "wishes", r.get("records"));
    }

    private Map<String, Object> manageWish(Map<String, Object> a) {
        if ("delete".equals(a.get("action"))) { api.deleteWish(num(a, "id"), token); return m("success", true, "message", "已删除"); }
        return m("success", false, "message", "未知操作");
    }

    // ── Site Config ──

    private Map<String, Object> getSiteConfig(Map<String, Object> a) {
        var r = api.listSiteConfigs(token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "配置已获取", "configs", r.get("records"));
    }

    private Map<String, Object> updateSiteConfig(Map<String, Object> a) {
        var body = new JSONObject();
        body.set("configKey", a.get("configKey"));
        body.set("configValue", a.get("configValue"));
        api.saveSiteConfig(body, token);
        return m("success", true, "message", "配置 " + a.get("configKey") + " 已更新");
    }

    // ── Categories ──

    private Map<String, Object> listCategories(Map<String, Object> a) {
        var r = api.listCategories(token);
        if (r == null) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "分类列表", "categories", r.get("records"));
    }

    // ── Work Experience ──

    private Map<String, Object> createWorkExperience(Map<String, Object> a) {
        var body = map(a, "company", "position", "location", "description", "startDate", "endDate");
        body.set("isCurrent", a.getOrDefault("isCurrent", false));
        body.set("status", 1);
        var r = api.createWorkExperience(body, token);
        if (r != null) return m("success", true, "message", "已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> deleteWorkExperience(Map<String, Object> a) {
        api.deleteWorkExperience(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    // ── Project ──

    private Map<String, Object> createProject(Map<String, Object> a) {
        var body = map(a, "name", "summary", "description", "role", "projectUrl");
        body.set("status", 1);
        if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
        var r = api.createProject(body, token);
        if (r != null) return m("success", true, "message", "已创建", "preview", preview(r));
        return m("success", false, "message", "创建失败");
    }

    private Map<String, Object> deleteProject(Map<String, Object> a) {
        api.deleteProject(num(a, "id"), token); return m("success", true, "message", "已删除");
    }

    // ── Dashboard ──

    private Map<String, Object> getDashboardStats(Map<String, Object> a) {
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
