package com.lune.agent.pipeline.tools;

import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 树洞工具处理器。 */
@Component
public class TreeHoleHandler implements ToolHandler {

    private final LuneApiClient api;

    public TreeHoleHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("list_treeholes", "delete_treehole");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        return switch (name) {
            case "list_treeholes" -> listResult(api.listTreeHoles(page(a), size(a), token), " 条", "treeholes");
            case "delete_treehole" -> deleted(api.deleteTreeHole(num(a, "id"), token));
            default -> m("success", false, "message", "Unknown treehole tool: " + name);
        };
    }
}
