package com.lune.agent.pipeline.tools;

import cn.hutool.json.JSONObject;
import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 网站统计工具处理器。 */
@Component
public class DashboardHandler implements ToolHandler {

    private final LuneApiClient api;

    public DashboardHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("get_dashboard_stats");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        return m("success", true, "message", "网站统计",
                "articleCount", count(() -> api.listArticles(1, 1, token)),
                "essayCount", count(() -> api.listEssays(1, 1, token)),
                "treeholeCount", count(() -> api.listTreeHoles(1, 1, token)),
                "wishCount", count(() -> api.listWishes(1, 1, token)));
    }

    private static Object count(Supplier<Optional<JSONObject>> s) {
        return s.get().map(o -> o.get("total")).orElse(0);
    }
}
