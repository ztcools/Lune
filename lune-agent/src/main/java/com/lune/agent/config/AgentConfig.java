package com.lune.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent LLM 配置（可被后台配置面板在运行时覆盖）。
 *
 * <p>apiKey 优先取自环境变量 {@code AGENT_API_KEY}（经 {@code app.agent.api-key} 绑定），
 * 运行时可被后台覆盖，重启后回落到环境变量值。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.agent")
public class AgentConfig {
    private String baseUrl = "https://aigw.phigent.cn";
    private String model = "claude-haiku-4-5-20251001";
    private String apiKey;
    private int maxTokens = 4096;
    private double temperature = 0.7;
    private int maxToolIterations = 8;
    private int sseTimeoutSeconds = 300;

    /** 是否已配置 API Key（单一判定来源，供 Orchestrator 与 LLMClient 复用）。 */
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
