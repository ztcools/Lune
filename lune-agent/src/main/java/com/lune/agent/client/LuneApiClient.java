package com.lune.agent.client;

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

@Component
public class LuneApiClient {

    private static final Logger log = LoggerFactory.getLogger(LuneApiClient.class);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;

    public LuneApiClient(@Value("${app.lune.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    private JSONObject get(String path, String token) {
        try {
            var r = http.send(request(path, token).GET().build(), HttpResponse.BodyHandlers.ofString());
            return parse(r);
        } catch (Exception e) { log.warn("GET {} err: {}", path, e.getMessage()); return null; }
    }

    private JSONObject post(String path, Object body, String token) {
        try {
            var r = http.send(request(path, token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSONUtil.toJsonStr(body)))
                    .build(), HttpResponse.BodyHandlers.ofString());
            return parse(r);
        } catch (Exception e) { log.warn("POST {} err: {}", path, e.getMessage()); return null; }
    }

    private JSONObject put(String path, Object body, String token) {
        try {
            var r = http.send(request(path, token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(JSONUtil.toJsonStr(body)))
                    .build(), HttpResponse.BodyHandlers.ofString());
            return parse(r);
        } catch (Exception e) { log.warn("PUT {} err: {}", path, e.getMessage()); return null; }
    }

    private void delete(String path, String token) {
        try {
            http.send(request(path, token).DELETE().build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { log.warn("DELETE {} err: {}", path, e.getMessage()); }
    }

    private HttpRequest.Builder request(String path, String token) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(30));
    }

    private JSONObject parse(HttpResponse<String> resp) {
        if (resp.statusCode() != 200) return null;
        var body = JSONUtil.parseObj(resp.body());
        if (body.getInt("code") != 200) return null;
        // data 可能是 JSONObject（分页结果）或 JSONArray（列表结果）
        var data = body.get("data");
        if (data instanceof JSONObject obj) return obj;
        if (data instanceof java.util.List) {
            var wrapper = new JSONObject();
            wrapper.set("records", data);
            return wrapper;
        }
        return null;
    }

    public JSONObject createArticle(JSONObject a, String t) { return post("/api/admin/articles", a, t); }
    public JSONObject updateArticle(Long id, JSONObject a, String t) { return put("/api/admin/articles/" + id, a, t); }
    public void deleteArticle(Long id, String t) { delete("/api/admin/articles/" + id, t); }
    public JSONObject listArticles(int page, int size, String t) { return get("/api/admin/articles?page=" + page + "&size=" + size, t); }
    public JSONObject createEssay(JSONObject e, String t) { return post("/api/admin/essays", e, t); }
    public void deleteEssay(Long id, String t) { delete("/api/admin/essays/" + id, t); }
    public JSONObject listEssays(int page, int size, String t) { return get("/api/admin/essays?page=" + page + "&size=" + size, t); }
    public JSONObject createRecord(JSONObject r, String t) { return post("/api/admin/records", r, t); }
    public void deleteRecord(Long id, String t) { delete("/api/admin/records/" + id, t); }
    public JSONObject listRecords(int page, int size, Long cid, String t) {
        var p = "/api/admin/records?page=" + page + "&size=" + size;
        if (cid != null) p += "&categoryId=" + cid; return get(p, t);
    }
    public JSONObject listTreeHoles(int page, int size, String t) { return get("/api/admin/treeholes?page=" + page + "&size=" + size, t); }
    public void deleteTreeHole(Long id, String t) { delete("/api/admin/treeholes/" + id, t); }
    public JSONObject listWishes(int page, int size, String t) { return get("/api/admin/wishes?page=" + page + "&size=" + size, t); }
    public void deleteWish(Long id, String t) { delete("/api/admin/wishes/" + id, t); }
    public JSONObject listSiteConfigs(String t) { return get("/api/admin/site-configs", t); }
    public JSONObject saveSiteConfig(JSONObject c, String t) { return post("/api/admin/site-configs", c, t); }
    public JSONObject listCategories(String t) { return get("/api/admin/categories", t); }
    public JSONObject createWorkExperience(JSONObject w, String t) { return post("/api/admin/resume/work", w, t); }
    public JSONObject updateWorkExperience(Long id, JSONObject w, String t) { return put("/api/admin/resume/work/" + id, w, t); }
    public void deleteWorkExperience(Long id, String t) { delete("/api/admin/resume/work/" + id, t); }
    public JSONObject listWorkExperiences(String t) { return get("/api/admin/resume/work", t); }
    public JSONObject createProject(JSONObject p, String t) { return post("/api/admin/resume/project", p, t); }
    public JSONObject updateProject(Long id, JSONObject p, String t) { return put("/api/admin/resume/project/" + id, p, t); }
    public void deleteProject(Long id, String t) { delete("/api/admin/resume/project/" + id, t); }
    public JSONObject listProjects(String t) { return get("/api/admin/resume/project", t); }
    public JSONObject uploadFromUrl(String url, String t) { return post("/api/admin/resources/upload-from-url", new JSONObject().set("url", url), t); }
}
