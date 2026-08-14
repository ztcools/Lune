package com.lune.agent.pipeline;

import java.util.List;
import java.util.Map;

/**
 * 工具处理器 —— 负责一组同名领域工具的执行。
 *
 * <p>每个领域一个 {@code @Component} 实现，Spring 通过 {@code List<ToolHandler>} 自动注入，
 * {@link ToolExecutor} 按 {@link #names()} 自动建注册表，无需手动 register。</p>
 */
public interface ToolHandler {

    /** 该 handler 负责的工具名列表。 */
    List<String> names();

    /** 执行指定工具。 */
    Map<String, Object> execute(String name, Map<String, Object> args, String token);
}
