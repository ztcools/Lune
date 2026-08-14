package com.lune.agent.pipeline.tools;

import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 许愿池工具处理器。 */
@Component
public class WishHandler implements ToolHandler {

    private final LuneApiClient api;

    public WishHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("list_wishes", "manage_wish");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        return switch (name) {
            case "list_wishes" -> listResult(api.listWishes(page(a), size(a), token), " 条", "wishes");
            case "manage_wish" -> {
                if ("delete".equals(a.get("action"))) yield deleted(api.deleteWish(num(a, "id"), token));
                yield m("success", false, "message", "未知操作");
            }
            default -> m("success", false, "message", "Unknown wish tool: " + name);
        };
    }
}
