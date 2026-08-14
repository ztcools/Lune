package com.lune.agent.pipeline.tools;

import cn.hutool.json.JSONObject;
import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 网站配置工具处理器。 */
@Component
public class ConfigHandler implements ToolHandler {

    private final LuneApiClient api;

    public ConfigHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("get_site_config", "update_site_config");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        return switch (name) {
            case "get_site_config" -> {
                var opt = api.listSiteConfigs(token);
                if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                yield m("success", true, "message", "配置已获取", "configs", opt.get().get("records"));
            }
            case "update_site_config" -> {
                var body = new JSONObject();
                body.set("configKey", a.get("configKey"));
                body.set("configValue", a.get("configValue"));
                if (api.saveSiteConfig(body, token).isPresent())
                    yield m("success", true, "message", "配置 " + a.get("configKey") + " 已更新");
                yield m("success", false, "message", "配置更新失败");
            }
            default -> m("success", false, "message", "Unknown config tool: " + name);
        };
    }
}
