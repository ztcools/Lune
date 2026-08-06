package com.lune.agent.controller;

import com.lune.agent.config.AgentConfig;
import com.lune.agent.dto.ChatRequest;
import com.lune.agent.memory.ChatMemory;
import com.lune.agent.pipeline.AgentOrchestrator;
import com.lune.agent.pipeline.ToolExecutor;
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
    private final ToolExecutor toolExecutor;

    public AgentController(AgentOrchestrator orchestrator, ChatMemory memory,
                           AgentConfig config, ToolExecutor toolExecutor) {
        this.orchestrator = orchestrator;
        this.memory = memory;
        this.config = config;
        this.toolExecutor = toolExecutor;
    }

    private Long resolveUserId(@RequestHeader(value = "X-User-Id", required = false) String uid) {
        if (uid != null) try { return Long.parseLong(uid); } catch (NumberFormatException e) { /* fall through */ }
        return 1L; // single-admin default
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest req,
                           @RequestHeader("Authorization") String auth,
                           @RequestHeader(value = "X-User-Id", required = false) String uid) {
        toolExecutor.setToken(extractToken(auth));
        return orchestrator.run(resolveUserId(uid), req.getMessage());
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        var k = config.getApiKey();
        var masked = (k != null && k.length() > 8) ? k.substring(0, 4) + "****" + k.substring(k.length() - 4) : k;
        return Map.of("code", 200, "data", Map.of(
                "provider", config.getProvider(),
                "baseUrl", config.getBaseUrl(),
                "model", config.getModel(),
                "apiKey", masked != null ? masked : ""
        ));
    }

    @PutMapping("/config")
    public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("baseUrl")) config.setBaseUrl((String) body.get("baseUrl"));
        if (body.containsKey("model")) config.setModel((String) body.get("model"));
        if (body.containsKey("apiKey")) config.setApiKey((String) body.get("apiKey"));
        return Map.of("code", 200, "message", "success");
    }

    @DeleteMapping("/history")
    public Map<String, Object> clearHistory() {
        memory.clear(1L);
        return Map.of("code", 200, "message", "success");
    }

    @GetMapping("/history")
    public Map<String, Object> getHistory() {
        return Map.of("code", 200, "data", memory.load(1L));
    }

    @PutMapping("/context")
    public Map<String, Object> setContext(@RequestParam boolean enabled) {
        memory.setContextEnabled(1L, enabled);
        return Map.of("code", 200, "message", "success");
    }

    private String extractToken(String auth) {
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return auth;
    }
}
