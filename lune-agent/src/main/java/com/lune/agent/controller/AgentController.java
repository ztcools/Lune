package com.lune.agent.controller;

import com.lune.agent.common.AgentException;
import com.lune.agent.config.AgentConfig;
import com.lune.agent.dto.ChatRequest;
import com.lune.agent.memory.ChatMemory;
import com.lune.agent.memory.UserPreference;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import com.lune.agent.pipeline.AgentOrchestrator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/agent")
public class AgentController {

    private final AgentOrchestrator orchestrator;
    private final ChatMemory memory;
    private final AgentConfig config;
    private final UserPreference preferences;

    public AgentController(AgentOrchestrator orchestrator, ChatMemory memory,
                           AgentConfig config, UserPreference preferences) {
        this.orchestrator = orchestrator;
        this.memory = memory;
        this.config = config;
        this.preferences = preferences;
    }

    /**
     * 从 JWT token（已由 JwtAuthFilter 注入 SecurityContext）提取 userId。
     * 不再依赖请求头或硬编码 fallback。
     */
    private Long resolveUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Claims claims) {
            Long userId = claims.get("userId", Long.class);
            if (userId != null) return userId;
        }
        throw new AgentException("无法识别用户身份，请重新登录");
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest req,
                           @RequestHeader("Authorization") String auth) {
        return orchestrator.run(resolveUserId(), req.getMessage(), extractToken(auth));
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        var k = config.getApiKey();
        // 仅暴露后 4 位，前缀替换为固定值，防止密钥猜测
        var masked = (k != null && k.length() > 4) ? "sk-****" + k.substring(k.length() - 4) : (k != null ? "****" : "");
        return Map.of("code", 200, "data", Map.of(
                "baseUrl", config.getBaseUrl(),
                "model", config.getModel(),
                "apiKey", masked
        ));
    }

    @PutMapping("/config")
    public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("baseUrl")) config.setBaseUrl((String) body.get("baseUrl"));
        if (body.containsKey("model")) config.setModel((String) body.get("model"));
        if (body.containsKey("apiKey")) {
            var key = (String) body.get("apiKey");
            // 忽略脱敏占位符（"sk-****xxxx"），避免前端把掩码回写覆盖真实密钥
            if (key != null && !key.isBlank() && !key.startsWith("sk-****")) {
                config.setApiKey(key);
            }
        }
        return Map.of("code", 200, "message", "success");
    }

    @DeleteMapping("/history")
    public Map<String, Object> clearHistory(@RequestParam(defaultValue = "default") String sessionId) {
        memory.clear(resolveUserId(), sessionId);
        return Map.of("code", 200, "message", "success");
    }

    @GetMapping("/history")
    public Map<String, Object> getHistory(@RequestParam(defaultValue = "default") String sessionId) {
        return Map.of("code", 200, "data", memory.load(resolveUserId(), sessionId));
    }

    @GetMapping("/sessions")
    public Map<String, Object> listSessions() {
        return Map.of("code", 200, "data", memory.listSessions(resolveUserId()));
    }

    @PutMapping("/context")
    public Map<String, Object> setContext(@RequestParam boolean enabled) {
        memory.setContextEnabled(resolveUserId(), enabled);
        return Map.of("code", 200, "message", "success");
    }

    @GetMapping("/preferences")
    public Map<String, Object> getPreferences() {
        var prefs = preferences.getAll(resolveUserId());
        return Map.of("code", 200, "data", prefs);
    }

    @PutMapping("/preferences")
    public Map<String, Object> savePreferences(@RequestBody Map<String, String> body) {
        preferences.setAll(resolveUserId(), body);
        return Map.of("code", 200, "message", "success");
    }

    private String extractToken(String auth) {
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return auth;
    }
}
