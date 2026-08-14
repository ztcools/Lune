package com.lune.agent.pipeline.tools;

import cn.hutool.json.JSONObject;
import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 文章工具处理器。 */
@Component
public class ArticleHandler implements ToolHandler {

    private final LuneApiClient api;

    public ArticleHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("create_article", "publish_article", "update_article", "delete_article", "list_articles");
    }

    @Override
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
                yield m("success", true, "message", "文章草稿已创建，说「发」即发布", "preview", preview(opt.get()));
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
            case "list_articles" -> listResult(api.listArticles(page(a), size(a), token), " 篇", "articles");
            default -> m("success", false, "message", "Unknown article tool: " + name);
        };
    }
}
