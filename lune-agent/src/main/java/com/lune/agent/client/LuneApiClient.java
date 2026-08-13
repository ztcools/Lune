package com.lune.agent.client;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Lune 后端 API 客户端。
 *
 * <p>统一封装 lune-web admin API 调用，返回 {@link Optional} 避免 null 传播。
 * GET/POST/PUT 返回 {@code Optional<JSONObject>}，DELETE 返回 boolean。</p>
 */
@Component
public class LuneApiClient {

    private static final Logger log = LoggerFactory.getLogger(LuneApiClient.class);
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_2)
            .build();
    private final String baseUrl;

    public LuneApiClient(@Value("${app.lune.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    // ── HTTP helpers ──

    private Optional<JSONObject> get(String path, String token) {
        try {
            var r = http.send(request(path, token).GET().build(), HttpResponse.BodyHandlers.ofString());
            return parse(r);
        } catch (Exception e) {
            log.warn("GET {} err: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JSONObject> post(String path, Object body, String token) {
        try {
            var r = http.send(request(path, token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSONUtil.toJsonStr(body)))
                    .build(), HttpResponse.BodyHandlers.ofString());
            return parse(r);
        } catch (Exception e) {
            log.warn("POST {} err: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JSONObject> put(String path, Object body, String token) {
        try {
            var r = http.send(request(path, token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(JSONUtil.toJsonStr(body)))
                    .build(), HttpResponse.BodyHandlers.ofString());
            return parse(r);
        } catch (Exception e) {
            log.warn("PUT {} err: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean delete(String path, String token) {
        try {
            var r = http.send(request(path, token).DELETE().build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) {
                log.warn("DELETE {} HTTP {}", path, r.statusCode());
                return false;
            }
            return JSONUtil.parseObj(r.body()).getInt("code") == 200;
        } catch (Exception e) {
            log.warn("DELETE {} err: {}", path, e.getMessage());
            return false;
        }
    }

    private HttpRequest.Builder request(String path, String token) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(30));
    }

    private Optional<JSONObject> parse(HttpResponse<String> resp) {
        if (resp.statusCode() != 200) {
            log.warn("HTTP {} from lune-web: {}", resp.statusCode(),
                    resp.body().length() > 200 ? resp.body().substring(0, 200) : resp.body());
            return Optional.empty();
        }
        var body = JSONUtil.parseObj(resp.body());
        if (body.getInt("code") != 200) {
            log.warn("Business error from lune-web: code={}, message={}", body.getInt("code"), body.getStr("message"));
            return Optional.empty();
        }
        var data = body.get("data");
        if (data instanceof JSONObject obj) return Optional.of(obj);
        if (data instanceof JSONArray arr) {
            var wrapper = new JSONObject();
            wrapper.set("records", arr);
            return Optional.of(wrapper);
        }
        // data 为 null 的接口（如 saveSiteConfig）
        return Optional.of(new JSONObject().set("_raw", body));
    }

    // ── Article ──

    public Optional<JSONObject> createArticle(JSONObject a, String t) { return post("/api/admin/articles", a, t); }
    public Optional<JSONObject> updateArticle(Long id, JSONObject a, String t) { return put("/api/admin/articles/" + id, a, t); }
    public boolean deleteArticle(Long id, String t) { return delete("/api/admin/articles/" + id, t); }
    public Optional<JSONObject> listArticles(int page, int size, String t) { return get("/api/admin/articles?page=" + page + "&size=" + size, t); }

    // ── Essay ──

    public Optional<JSONObject> createEssay(JSONObject e, String t) { return post("/api/admin/essays", e, t); }
    public boolean deleteEssay(Long id, String t) { return delete("/api/admin/essays/" + id, t); }
    public Optional<JSONObject> listEssays(int page, int size, String t) { return get("/api/admin/essays?page=" + page + "&size=" + size, t); }

    // ── Record ──

    public Optional<JSONObject> createRecord(JSONObject r, String t) { return post("/api/admin/records", r, t); }
    public boolean deleteRecord(Long id, String t) { return delete("/api/admin/records/" + id, t); }
    public Optional<JSONObject> listRecords(int page, int size, Long cid, String t) {
        var p = "/api/admin/records?page=" + page + "&size=" + size;
        if (cid != null) p += "&categoryId=" + cid;
        return get(p, t);
    }

    // ── TreeHole ──

    public Optional<JSONObject> listTreeHoles(int page, int size, String t) { return get("/api/admin/treeholes?page=" + page + "&size=" + size, t); }
    public boolean deleteTreeHole(Long id, String t) { return delete("/api/admin/treeholes/" + id, t); }

    // ── Wish ──

    public Optional<JSONObject> listWishes(int page, int size, String t) { return get("/api/admin/wishes?page=" + page + "&size=" + size, t); }
    public boolean deleteWish(Long id, String t) { return delete("/api/admin/wishes/" + id, t); }

    // ── Site Config ──

    public Optional<JSONObject> listSiteConfigs(String t) { return get("/api/admin/site-configs", t); }
    public Optional<JSONObject> saveSiteConfig(JSONObject c, String t) { return post("/api/admin/site-configs", c, t); }

    // ── Categories ──

    public Optional<JSONObject> listCategories(String t) { return get("/api/admin/categories", t); }

    // ── Work Experience ──

    public Optional<JSONObject> createWorkExperience(JSONObject w, String t) { return post("/api/admin/resume/work", w, t); }
    public Optional<JSONObject> updateWorkExperience(Long id, JSONObject w, String t) { return put("/api/admin/resume/work/" + id, w, t); }
    public boolean deleteWorkExperience(Long id, String t) { return delete("/api/admin/resume/work/" + id, t); }
    public Optional<JSONObject> listWorkExperiences(String t) { return get("/api/admin/resume/work", t); }

    // ── Project ──

    public Optional<JSONObject> createProject(JSONObject p, String t) { return post("/api/admin/resume/project", p, t); }
    public Optional<JSONObject> updateProject(Long id, JSONObject p, String t) { return put("/api/admin/resume/project/" + id, p, t); }
    public boolean deleteProject(Long id, String t) { return delete("/api/admin/resume/project/" + id, t); }
    public Optional<JSONObject> listProjects(String t) { return get("/api/admin/resume/project", t); }

    // ── Resource ──

    public Optional<JSONObject> uploadFromUrl(String url, String t) {
        return post("/api/admin/resources/upload-from-url", new JSONObject().set("url", url), t);
    }
}
