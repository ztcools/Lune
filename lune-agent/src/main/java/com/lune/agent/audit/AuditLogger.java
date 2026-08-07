package com.lune.agent.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Agent 操作审计日志。
 *
 * <p>记录每条工具调用的用户、时间、工具名、参数摘要、结果状态。
 * 通过 SLF4J 输出，可接入日志收集系统（ELK/Loki/CloudWatch）。</p>
 */
@Component
public class AuditLogger {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    public void logToolCall(Long userId, String toolName, Map<String, Object> args, boolean success, String message) {
        var summary = buildArgSummary(args);
        audit.info("user={} tool={} success={} args={} result={}",
                userId, toolName, success, summary,
                message != null && message.length() > 100 ? message.substring(0, 100) : message);
    }

    public void logChatStart(Long userId, String messagePreview) {
        var preview = messagePreview != null && messagePreview.length() > 80
                ? messagePreview.substring(0, 80) + "..." : messagePreview;
        audit.info("user={} action=chat_start message={}", userId, preview);
    }

    public void logChatEnd(Long userId, int toolCalls, long durationMs, boolean success) {
        audit.info("user={} action=chat_end toolCalls={} durationMs={} success={}",
                userId, toolCalls, durationMs, success);
    }

    private String buildArgSummary(Map<String, Object> args) {
        if (args == null || args.isEmpty()) return "{}";
        var sb = new StringBuilder("{");
        for (var entry : args.entrySet()) {
            if (sb.length() > 80) { sb.append("..."); break; }
            if (sb.length() > 1) sb.append(", ");
            sb.append(entry.getKey()).append("=");
            var val = String.valueOf(entry.getValue());
            sb.append(val.length() > 30 ? val.substring(0, 30) + "..." : val);
        }
        sb.append("}");
        return sb.toString();
    }
}
