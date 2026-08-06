package com.lune.agent.llm;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.config.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class LLMClient {

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final AgentConfig config;

    public LLMClient(AgentConfig config) {
        this.config = config;
    }

    public JSONObject chat(JSONArray messages, JSONArray tools) {
        return call(messages, tools);
    }

    private JSONObject call(JSONArray messages, JSONArray tools) {
        String apiKey = resolveApiKey();
        if (apiKey == null) return null;

        try {
            var body = new JSONObject();
            body.set("model", config.getModel());
            body.set("messages", messages);
            body.set("stream", false);
            body.set("max_tokens", config.getMaxTokens());
            body.set("temperature", config.getTemperature());
            if (tools != null && !tools.isEmpty()) {
                body.set("tools", tools);
            }

            var uri = config.getBaseUrl().replaceAll("/+$", "") + "/v1/chat/completions";
            var req = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.error("LLM error {}: {}", resp.statusCode(),
                        resp.body().length() > 500 ? resp.body().substring(0, 500) : resp.body());
                return null;
            }
            return JSONUtil.parseObj(resp.body());
        } catch (Exception e) {
            log.error("LLM call failed: {}", e.getMessage());
            return null;
        }
    }

    private String resolveApiKey() {
        var key = config.getApiKey();
        if (key != null && !key.isBlank()) return key;
        // Fallback to env var
        key = System.getenv("AGENT_API_KEY");
        return (key != null && !key.isBlank()) ? key : null;
    }
}
