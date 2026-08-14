package com.lune.agent.pipeline.tools;

import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 记录工具处理器。 */
@Component
public class RecordHandler implements ToolHandler {

    private final LuneApiClient api;

    public RecordHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("create_record", "delete_record", "list_records");
    }

    @Override
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
                Long cid = a.containsKey("categoryId") ? num(a, "categoryId") : null;
                yield listResult(api.listRecords(page(a), size(a), cid, token), " 条", "records");
            }
            default -> m("success", false, "message", "Unknown record tool: " + name);
        };
    }
}
