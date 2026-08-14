package com.lune.agent.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.m;

/**
 * 工具执行器 —— 注册表模式（自动注册）。
 *
 * <p>各领域 {@link ToolHandler} 以 {@code @Component} 独立实现（见 {@code pipeline.tools} 包），
 * Spring 注入 {@code List<ToolHandler>} 后按 {@link ToolHandler#names()} 自动建注册表。
 * 新增工具只需新增一个 handler 组件 + 在 {@link ToolDefinitions} 加一条定义。</p>
 */
@Component
public class ToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);

    private final Map<String, ToolHandler> handlers = new LinkedHashMap<>();

    public ToolExecutor(List<ToolHandler> handlerBeans) {
        for (var handler : handlerBeans) {
            for (var name : handler.names()) handlers.put(name, handler);
        }
        log.info("Registered {} tools via {} handlers", handlers.size(), handlerBeans.size());
    }

    /**
     * 执行工具调用。超时由 {@link com.lune.agent.client.LuneApiClient} 的 HTTP 客户端（30s）兜底，
     * 此处直接同步执行，避免嵌套线程池。
     */
    public Map<String, Object> execute(String name, Map<String, Object> args, String token) {
        var handler = handlers.get(name);
        if (handler == null) return m("success", false, "message", "未知工具: " + name);
        try {
            return handler.execute(name, args, token);
        } catch (Exception e) {
            log.error("Tool {} error: {}", name, e.getMessage(), e);
            return m("success", false, "message", "执行失败: " + e.getMessage());
        }
    }
}
