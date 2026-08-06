package com.lune.agent.pipeline;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.config.AgentConfig;
import com.lune.agent.dto.ChatMessage;
import com.lune.agent.llm.LLMClient;
import com.lune.agent.memory.ChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main agent pipeline: Intent → Plan → Tool Selection → Execution → Reflection → Response
 *
 * Flow:
 * 1. Build context from memory + system prompt + tool definitions
 * 2. Call LLM with tools
 * 3. If tool_calls: execute tools, feed results back, repeat (max N iterations)
 * 4. Stream final response via SSE
 */
@Component
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final LLMClient llm;
    private final ChatMemory memory;
    private final ToolExecutor toolExecutor;
    private final AgentConfig config;

    public AgentOrchestrator(LLMClient llm, ChatMemory memory,
                             ToolExecutor toolExecutor, AgentConfig config) {
        this.llm = llm;
        this.memory = memory;
        this.toolExecutor = toolExecutor;
        this.config = config;
    }

    public SseEmitter run(Long userId, String userMessage) {
        if (resolveApiKey() == null) {
            return errorEmitter("请先配置 API Key。点击右上角齿轮图标进行配置。");
        }
        var emitter = new SseEmitter((long) config.getSseTimeoutSeconds() * 1000);

        new Thread(() -> {
            try {
                orchestrate(emitter, userId, userMessage);
            } catch (Exception e) {
                log.error("Pipeline error user={}: {}", userId, e.getMessage(), e);
                safeSend(emitter, event("error", "处理出错: " + e.getMessage()));
                emitter.complete();
            }
        }, "agent-pipe-" + userId).start();

        return emitter;
    }

    private void orchestrate(SseEmitter emitter, Long userId, String userMessage) {
        // ── Load context ──
        List<ChatMessage> history = memory.isContextEnabled(userId) ? memory.load(userId) : new ArrayList<>();
        history.add(new ChatMessage("user", userMessage, null, null, null, LocalDateTime.now()));

        // ── Build messages ──
        JSONArray messages = new JSONArray();
        messages.add(systemMessage());
        for (var h : history) {
            if ("tool".equals(h.getRole())) continue; // skip tool msgs when rebuilding
            var m = new JSONObject();
            m.set("role", h.getRole());
            m.set("content", h.getContent() != null ? h.getContent() : "");
            messages.add(m);
        }

        // ── Tool loop ──
        JSONArray tools = toolExecutor.getDefinitions();
        for (int iter = 0; iter < config.getMaxToolIterations(); iter++) {
            var result = llm.chat(messages, tools);
            if (result == null) {
                safeSend(emitter, event("error", "LLM 调用失败"));
                emitter.complete(); return;
            }

            var choice = result.getJSONArray("choices").getJSONObject(0);
            String finishReason = choice.getStr("finish_reason");

            if ("tool_calls".equals(finishReason)) {
                // ── Execute tools ──
                var msgContent = choice.getByPath("message.content", String.class);
                var toolCalls = choice.getByPath("message.tool_calls", JSONArray.class);

                // Add assistant message with tool_calls to context
                var asst = new JSONObject();
                asst.set("role", "assistant");
                asst.set("content", msgContent != null ? msgContent : "");
                var reasoning = choice.getByPath("message.reasoning_content", String.class);
                if (reasoning != null && !reasoning.isEmpty()) asst.set("reasoning_content", reasoning);
                asst.set("tool_calls", toolCalls);
                messages.add(asst);

                history.add(new ChatMessage("assistant", msgContent, null, null, null, LocalDateTime.now()));

                // Run each tool
                for (int i = 0; i < toolCalls.size(); i++) {
                    var tc = toolCalls.getJSONObject(i);
                    var fn = tc.getJSONObject("function");
                    String name = fn.getStr("name");
                    String callId = tc.getStr("id");
                    Map<String, Object> args;
                    try { args = JSONUtil.toBean(fn.getStr("arguments"), Map.class); }
                    catch (Exception e) { args = Map.of(); }

                    safeSend(emitter, event("tool_call", Map.of("toolName", name, "toolCallId", callId, "args", args)));

                    Map<String, Object> toolResult = toolExecutor.execute(name, args);

                    safeSend(emitter, event("tool_result", buildToolResult(name, toolResult)));

                    // Add tool response
                    var tool = new JSONObject();
                    tool.set("role", "tool");
                    tool.set("tool_call_id", callId);
                    tool.set("content", JSONUtil.toJsonStr(toolResult));
                    messages.add(tool);

                    history.add(new ChatMessage("tool", null, callId, name, toolResult, LocalDateTime.now()));
                }
                continue; // next iteration
            }

            // ── Text response ──
            String content = choice.getByPath("message.content", String.class);
            if (content != null && !content.isEmpty()) {
                safeSend(emitter, event("text", Map.of("content", content)));
                history.add(new ChatMessage("assistant", content, null, null, null, LocalDateTime.now()));
            }

            if ("length".equals(finishReason)) {
                safeSend(emitter, event("text", Map.of("content", "\n\n（回复被截断，尝试缩短你的问题）")));
            }
            break;
        }

        // ── Save memory ──
        memory.save(userId, history);
        safeSend(emitter, event("done", Map.of()));
        emitter.complete();
    }

    private Map<String, Object> buildToolResult(String name, Map<String, Object> result) {
        var m = new HashMap<>(result);
        m.put("_toolName", name);
        return m;
    }

    // ── System prompt ──

    private JSONObject systemMessage() {
        var sys = new JSONObject();
        sys.set("role", "system");
        sys.set("content", """
            你是 Luna，博主的网站助手。用工具做事，别空回复。
            - 信息不够就问。建文章用 create_article 建草稿，记住 id。用户说"发"直接用 id 调 publish_article。
            - 删前确认。数据用工具查。
            """);
        return sys;
    }

    // ── Helpers ──

    private String resolveApiKey() {
        var key = config.getApiKey();
        if (key != null && !key.isBlank()) return key;
        key = System.getenv("AGENT_API_KEY");
        if (key != null && !key.isBlank()) return key;
        // Also try Redis-stored config (set by frontend)
        return null;
    }

    private String event(String type) { return event(type, null); }

    private String event(String type, Object data) {
        var obj = new JSONObject();
        obj.set("type", type);
        if (data != null) obj.set("data", data);
        return obj.toString();
    }

    private void safeSend(SseEmitter emitter, String data) {
        try { emitter.send(data); } catch (IOException e) { log.debug("SSE client disconnect"); }
    }

    private SseEmitter errorEmitter(String msg) {
        var e = new SseEmitter();
        e.onCompletion(() -> {});
        try { e.send(event("error", msg)); e.complete(); }
        catch (IOException ex) { /* ignore */ }
        return e;
    }
}
