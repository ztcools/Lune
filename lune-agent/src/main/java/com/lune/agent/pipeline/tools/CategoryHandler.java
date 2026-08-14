package com.lune.agent.pipeline.tools;

import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 分类工具处理器。 */
@Component
public class CategoryHandler implements ToolHandler {

    private final LuneApiClient api;

    public CategoryHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("list_categories");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        var opt = api.listCategories(token);
        if (opt.isEmpty()) return m("success", false, "message", "查询失败");
        return m("success", true, "message", "分类列表", "categories", opt.get().get("records"));
    }
}
