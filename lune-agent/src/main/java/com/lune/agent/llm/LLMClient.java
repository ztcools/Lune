package com.lune.agent.llm;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.config.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * DeepSeek 兼容 LLM 客户端。
 *
 * <p>支持流式（{@link #chatStream}）和非流式（{@link #chat}）两种调用模式。
 * 流式模式逐 chunk 回调，首个 token 延迟显著低于非流式。
 * 内置指数退避重试（最多 3 次）和超时控制。</p>
 */
@Component
public class LLMClient {

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BASE_MS = 1000;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final AgentConfig config;

    public LLMClient(AgentConfig config) {
        this.config = config;
    }

    // ── Non-streaming (保持兼容，用于 tool_calls 场景) ──

    public JSONObject chat(JSONArray messages, JSONArray tools) {
        return callWithRetry(messages, tools, false, null);
    }

    // ── Streaming ──

    /**
     * 流式调用 LLM，每个文本增量通过 {@code onChunk} 回调，最终返回完整的响应 JSON。
     *
     * <p>使用场景：AgentOrchestrator 先尝试流式输出文本；如果模型返回 tool_calls，
     * 流式 SSE chunk 中包含 tool_calls 数据，最终收集后执行工具循环。</p>
     *
     * @param messages 消息列表
     * @param tools    工具定义（可为 null）
     * @param onChunk  文本增量回调（不包含 tool_calls chunk）
     * @return 完整响应 JSON，或 null（调用失败）
     */
    public JSONObject chatStream(JSONArray messages, JSONArray tools, Consumer<String> onChunk) {
        return callWithRetry(messages, tools, true, onChunk);
    }

    // ── Core ──

    private JSONObject callWithRetry(JSONArray messages, JSONArray tools, boolean stream,
                                     Consumer<String> onChunk) {
        long delay = RETRY_BASE_MS;
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return doCall(messages, tools, stream, onChunk);
            } catch (Exception e) {
                log.warn("LLM call attempt {}/{} failed: {}", attempt + 1, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return null; }
                    delay *= 2;
                }
            }
        }
        log.error("LLM call exhausted {} retries", MAX_RETRIES);
        return null;
    }

    private JSONObject doCall(JSONArray messages, JSONArray tools, boolean stream,
                              Consumer<String> onChunk) throws Exception {
        String apiKey = resolveApiKey();
        if (apiKey == null) return null;

        var uri = config.getBaseUrl().replaceAll("/+$", "") + "/v1/chat/completions";
        var body = new JSONObject();
        body.set("model", config.getModel());
        body.set("messages", messages);
        body.set("stream", stream);
        body.set("max_tokens", config.getMaxTokens());
        body.set("temperature", config.getTemperature());
        if (tools != null && !tools.isEmpty()) {
            body.set("tools", tools);
        }

        var bodyStr = body.toString();
        log.debug("LLM {} request: {} msgs, {} tools, {} bytes → {}",
                stream ? "streaming" : "batch", messages.size(),
                tools != null ? tools.size() : 0, bodyStr.length(), uri);

        var req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(stream ? 180 : 120))
                .POST(HttpRequest.BodyPublishers.ofString(bodyStr))
                .build();

        if (stream) {
            return handleStreamingResponse(req, onChunk);
        } else {
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.error("LLM error {}: {}", resp.statusCode(),
                        resp.body().length() > 500 ? resp.body().substring(0, 500) : resp.body());
                return null;
            }
            return JSONUtil.parseObj(resp.body());
        }
    }

    /**
     * 处理流式 SSE 响应。
     *
     * <p>解析 SSE {@code data: ...} 行，提取文本增量通过 {@code onChunk} 回调，
     * 同时收集完整内容、tool_calls 等字段，最终返回合并后的完整响应 JSON。</p>
     */
    private JSONObject handleStreamingResponse(HttpRequest req, Consumer<String> onChunk) throws Exception {
        var resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            // 读取错误 body
            var body = new String(resp.body().readAllBytes());
            log.error("LLM stream error {}: {}", resp.statusCode(),
                    body.length() > 500 ? body.substring(0, 500) : body);
            return null;
        }

        var merged = new JSONObject();
        var choices = new JSONArray();
        var mergedChoice = new JSONObject();
        var mergedDelta = new JSONObject();
        var mergedToolCalls = new JSONArray();
        int index = 0;

        try (var reader = new BufferedReader(new InputStreamReader(resp.body()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6);
                if ("[DONE]".equals(data)) break;

                try {
                    var chunk = JSONUtil.parseObj(data);
                    // 混元等模型可能通过 chunk.choices 的顶层 null 表示结束
                    var chunkChoices = chunk.getJSONArray("choices");
                    if (chunkChoices == null || chunkChoices.isEmpty()) continue;

                    // 收集 usage（通常在最后一个 chunk）
                    if (chunk.containsKey("usage")) {
                        merged.set("usage", chunk.get("usage"));
                    }

                    for (int ci = 0; ci < chunkChoices.size(); ci++) {
                        var choice = chunkChoices.getJSONObject(ci);
                        int choiceIndex = choice.getInt("index", 0);
                        if (choiceIndex != index) continue;

                        var delta = choice.getJSONObject("delta");
                        if (delta == null) continue;
                        done:
                        {
                            // 文本增量
                            var deltaContent = delta.getJSONObject("content");
                            if (deltaContent != null && !deltaContent.isEmpty()) {
                                // 非 string 内容（结构化输出）→ 不回调，仅收集
                                break done;
                            }
                            String content = delta.getStr("content");
                            if (content != null && !content.isEmpty()) {
                                // 收集到 mergedDelta
                                var existing = mergedDelta.getStr("content");
                                mergedDelta.set("content", (existing != null ? existing : "") + content);
                                onChunk.accept(content);
                            }

                            // tool_calls 增量
                            var deltaToolCalls = delta.getJSONArray("tool_calls");
                            if (deltaToolCalls != null) {
                                for (int ti = 0; ti < deltaToolCalls.size(); ti++) {
                                    var dtc = deltaToolCalls.getJSONObject(ti);
                                    int tcIndex = dtc.getInt("index", 0);
                                    // 补齐 mergedToolCalls 数组
                                    while (mergedToolCalls.size() <= tcIndex) {
                                        var newTc = new JSONObject();
                                        newTc.set("index", mergedToolCalls.size());
                                        newTc.set("function", new JSONObject());
                                        mergedToolCalls.add(newTc);
                                    }
                                    var target = mergedToolCalls.getJSONObject(tcIndex);
                                    if (dtc.containsKey("id")) {
                                        target.set("id", dtc.getStr("id"));
                                        target.set("type", "function");
                                    }
                                    var fn = dtc.getJSONObject("function");
                                    if (fn != null) {
                                        var targetFn = target.getJSONObject("function");
                                        if (fn.containsKey("name")) targetFn.set("name", fn.getStr("name"));
                                        if (fn.containsKey("arguments")) {
                                            var existingArgs = targetFn.getStr("arguments");
                                            targetFn.set("arguments", (existingArgs != null ? existingArgs : "") + fn.getStr("arguments"));
                                        }
                                    }
                                }
                            }
                        }

                        // 收集 finish_reason
                        if (choice.containsKey("finish_reason") && choice.get("finish_reason") != null) {
                            mergedChoice.set("finish_reason", choice.getStr("finish_reason"));
                        }
                    }
                } catch (Exception e) {
                    log.debug("Skip malformed SSE chunk: {}", data.length() > 100 ? data.substring(0, 100) : data);
                }
            }
        }

        // 组装最终响应
        if (!mergedToolCalls.isEmpty()) {
            mergedDelta.set("tool_calls", mergedToolCalls);
        }
        mergedChoice.set("delta", null); // 清理 delta（对齐非流式结构：message 在下一层）
        mergedChoice.set("message", mergedDelta);
        mergedChoice.set("index", index);
        choices.add(mergedChoice);
        merged.set("choices", choices);

        return merged;
    }

    private String resolveApiKey() {
        var key = config.getApiKey();
        if (key != null && !key.isBlank()) return key;
        key = System.getenv("AGENT_API_KEY");
        return (key != null && !key.isBlank()) ? key : null;
    }
}
