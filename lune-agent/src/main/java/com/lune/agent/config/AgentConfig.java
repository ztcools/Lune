package com.lune.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.agent")
public class AgentConfig {
    private String provider = "deepseek";
    private String baseUrl = "https://aigw.phigent.cn";
    private String model = "deepseek/deepseek-v4-flash";
    private String apiKey;
    private int maxTokens = 4096;
    private double temperature = 0.7;
    private int maxToolIterations = 5;
    private int sseTimeoutSeconds = 300;
}
