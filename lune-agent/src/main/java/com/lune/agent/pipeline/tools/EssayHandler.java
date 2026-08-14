package com.lune.agent.pipeline.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 随笔工具处理器。 */
@Component
public class EssayHandler implements ToolHandler {

    private final LuneApiClient api;

    public EssayHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("create_essay", "delete_essay", "list_essays");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        return switch (name) {
            case "create_essay" -> {
                var body = mapObj(a, "content", "mood", "weather", "location", "media");
                if (a.containsKey("media") && a.get("media") instanceof String s) {
                    normalizeMedia(s, body);
                }
                body.set("status", 1);
                var opt = api.createEssay(body, token);
                if (opt.isPresent()) yield m("success", true, "message", "随笔已创建", "preview", preview(opt.get()));
                yield m("success", false, "message", "创建失败");
            }
            case "delete_essay" -> deleted(api.deleteEssay(num(a, "id"), token));
            case "list_essays" -> listResult(api.listEssays(page(a), size(a), token), " 条", "essays");
            default -> m("success", false, "message", "Unknown essay tool: " + name);
        };
    }

    /** 将媒体 JSON（可能是字符串数组或对象数组）规范化为 [{type,url}] 写入 body。 */
    private static void normalizeMedia(String s, JSONObject body) {
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
}
