package com.lune.agent.pipeline;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lune.agent.config.AgentConfig;
import com.lune.agent.dto.ChatMessage;
import com.lune.agent.llm.LLMClient;
import com.lune.agent.memory.ChatMemory;
import com.lune.agent.memory.UserPreference;
import com.lune.agent.profiles.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Agent 总控 —— 意图识别 → 路由到领域 Agent → 工具循环 → 响应。
 *
 * <p>6 个领域 Agent + 1 个通用兜底。每个领域有独立的 system prompt 和工具白名单。
 * 意图识别优先关键词匹配（零 LLM 调用），未命中时回退到 GeneralProfile。</p>
 */
@Component
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final LLMClient llm;
    private final ChatMemory memory;
    private final UserPreference preferences;
    private final ToolExecutor toolExecutor;
    private final AgentConfig config;

    public AgentOrchestrator(LLMClient llm, ChatMemory memory, UserPreference preferences,
                             ToolExecutor toolExecutor, AgentConfig config) {
        this.llm = llm;
        this.memory = memory;
        this.preferences = preferences;
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

    private void orchestrate(SseEmitter emitter, Long userId, String rawMessage) {
        // ── 快捷指令解析 ──
        var parsed = parsePrefix(rawMessage);
        String userMessage = parsed.message;
        AgentProfile forcedProfile = parsed.forcedProfile;

        // ── 意图路由 ──
        AgentProfile profile;
        if (forcedProfile != null) {
            // 如果强制路由后消息仍含其他领域关键词→多领域请求→通用Agent
            var intentProfile = routeIntent(userMessage);
            if (intentProfile.name().equals(forcedProfile.name()) || "general".equals(intentProfile.name())) {
                profile = forcedProfile;
                log.debug("Forced profile via prefix: {}", profile.name());
            } else {
                profile = GeneralProfile.create();
                log.debug("Multi-domain detected after prefix → general");
            }
        } else {
            profile = routeIntent(userMessage);
            log.debug("Intent routed to: {}", profile.name());
        }

        // ── 加载偏好 + 构建 system prompt ──
        Map<String, String> prefs = preferences.getAll(userId);
        String systemContent = profile.buildPrompt(prefs);

        // ── 获取工具 ──
        JSONArray tools = ToolDefinitions.filterByNames(profile.toolNames());

        // ── Load context ──
        List<ChatMessage> history = memory.isContextEnabled(userId) ? memory.load(userId) : new ArrayList<>();
        history.add(new ChatMessage("user", userMessage, null, null, null, LocalDateTime.now()));

        // ── Build messages ──
        JSONArray messages = new JSONArray();
        var sys = new JSONObject();
        sys.set("role", "system");
        sys.set("content", systemContent);
        messages.add(sys);
        for (var h : history) {
            if ("tool".equals(h.getRole())) continue;
            var m = new JSONObject();
            m.set("role", h.getRole());
            m.set("content", h.getContent() != null ? h.getContent() : "");
            messages.add(m);
        }

        // ── Tool loop ──
        for (int iter = 0; iter < config.getMaxToolIterations(); iter++) {
            var result = llm.chat(messages, tools);
            if (result == null) {
                safeSend(emitter, event("error", "LLM 调用失败"));
                emitter.complete(); return;
            }

            var choice = result.getJSONArray("choices").getJSONObject(0);
            String finishReason = choice.getStr("finish_reason");

            if ("tool_calls".equals(finishReason)) {
                var msgContent = choice.getByPath("message.content", String.class);
                var toolCalls = choice.getByPath("message.tool_calls", JSONArray.class);

                var asst = new JSONObject();
                asst.set("role", "assistant");
                asst.set("content", msgContent != null ? msgContent : "");
                var reasoning = choice.getByPath("message.reasoning_content", String.class);
                if (reasoning != null && !reasoning.isEmpty()) asst.set("reasoning_content", reasoning);
                asst.set("tool_calls", toolCalls);
                messages.add(asst);

                history.add(new ChatMessage("assistant", msgContent, null, null, null, LocalDateTime.now()));

                for (int i = 0; i < toolCalls.size(); i++) {
                    var tc = toolCalls.getJSONObject(i);
                    var fn = tc.getJSONObject("function");
                    String name = fn.getStr("name");
                    String callId = tc.getStr("id");
                    Map<String, Object> args;
                    try { args = JSONUtil.toBean(fn.getStr("arguments"), Map.class); }
                    catch (Exception e) { args = Map.of(); }

                    // 注入偏好默认值
                    args = injectPreferences(args, name, prefs);

                    safeSend(emitter, event("tool_call", Map.of("toolName", name, "toolCallId", callId, "args", args)));

                    Map<String, Object> toolResult = toolExecutor.execute(name, args);

                    safeSend(emitter, event("tool_result", buildToolResult(name, toolResult)));

                    var tool = new JSONObject();
                    tool.set("role", "tool");
                    tool.set("tool_call_id", callId);
                    tool.set("content", JSONUtil.toJsonStr(toolResult));
                    messages.add(tool);

                    history.add(new ChatMessage("tool", null, callId, name, toolResult, LocalDateTime.now()));
                }
                continue;
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

        if (memory.isContextEnabled(userId)) {
            memory.save(userId, history);
        }
        safeSend(emitter, event("done", Map.of()));
        emitter.complete();
    }

    // ── Intent routing ──

    /**
     * 解析 @文章/@随笔/@记录/@工作/@项目 前缀。
     * 返回 (actualMessage, forcedProfile)。
     */
    static ParsedMessage parsePrefix(String raw) {
        if (raw == null) return new ParsedMessage(raw, null);
        var prefixes = Map.of(
            "@文章", ArticleProfile.create(),
            "@随笔", EssayProfile.create(),
            "@记录", RecordProfile.create(),
            "@工作", WorkProfile.create(),
            "@项目", ProjectProfile.create()
        );
        for (var entry : prefixes.entrySet()) {
            if (raw.startsWith(entry.getKey())) {
                return new ParsedMessage(raw.substring(entry.getKey().length()).trim(), entry.getValue());
            }
        }
        return new ParsedMessage(raw, null);
    }

    record ParsedMessage(String message, AgentProfile forcedProfile) {}

    /**
     * 关键词意图路由（零 LLM 调用）。
     */
    static AgentProfile routeIntent(String msg) {
        if (msg == null || msg.isBlank()) return GeneralProfile.create();
        var lower = msg.toLowerCase();

        // 多领域请求 → 通用Agent（全工具可用）
        int domains = 0;
        if (containsAny(lower, "文章", "发文", "写一", "博客", "发布文章", "post")) domains++;
        if (containsAny(lower, "随笔", "朋友圈", "动态", "心情", "浮生记")) domains++;
        if (containsAny(lower, "记录", "收藏", "打卡", "看了", "读了", "光阴集")) domains++;
        if (containsAny(lower, "工作", "实习", "经历", "履痕", "公司", "上班")) domains++;
        if (containsAny(lower, "项目", "造物集", "开源", "github")) domains++;
        if (domains >= 2) return GeneralProfile.create();

        if (domains == 1) {
            if (containsAny(lower, "文章", "发文", "写一", "博客", "发布文章", "post"))
                return ArticleProfile.create();
            if (containsAny(lower, "随笔", "朋友圈", "动态", "心情", "浮生记"))
                return EssayProfile.create();
            if (containsAny(lower, "记录", "收藏", "打卡", "看了", "读了", "光阴集"))
                return RecordProfile.create();
            if (containsAny(lower, "工作", "实习", "经历", "履痕", "公司", "上班"))
                return WorkProfile.create();
            if (containsAny(lower, "项目", "造物集", "开源", "github"))
                return ProjectProfile.create();
        }

        return GeneralProfile.create();
    }

    private static boolean containsAny(String text, String... keywords) {
        for (var kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    // ── Preference injection ──

    /**
     * 将用户偏好注入为工具调用的默认参数（仅当参数未提供时）。
     */
    static Map<String, Object> injectPreferences(Map<String, Object> args, String toolName,
                                                  Map<String, String> prefs) {
        if (prefs == null || prefs.isEmpty()) return args;
        var mutable = new HashMap<>(args);
        switch (toolName) {
            case "create_essay" -> {
                if (!mutable.containsKey("location") && prefs.containsKey("essay_default_location")) {
                    mutable.put("location", prefs.get("essay_default_location"));
                }
            }
        }
        return mutable;
    }

    // ── Helpers ──

    private Map<String, Object> buildToolResult(String name, Map<String, Object> result) {
        var m = new HashMap<>(result);
        m.put("_toolName", name);
        return m;
    }

    private String resolveApiKey() {
        var key = config.getApiKey();
        if (key != null && !key.isBlank()) return key;
        key = System.getenv("AGENT_API_KEY");
        return (key != null && !key.isBlank()) ? key : null;
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
