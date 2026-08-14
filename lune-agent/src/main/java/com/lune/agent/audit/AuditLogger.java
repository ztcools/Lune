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
        // 只记参数键名不记值，避免正文/PII/密钥落日志
        var argKeys = args == null ? "[]" : args.keySet().toString();
        audit.info("user={} tool={} success={} argKeys={} result={}",
                userId, toolName, success, argKeys,
                message != null && message.length() > 100 ? message.substring(0, 100) : message);
    }

    public void logChatStart(Long userId, String messagePreview) {
        // 只记长度不记内容，避免用户消息正文落日志
        audit.info("user={} action=chat_start msgLen={}",
                userId, messagePreview == null ? 0 : messagePreview.length());
    }

    public void logChatEnd(Long userId, int toolCalls, long durationMs, boolean success) {
        audit.info("user={} action=chat_end toolCalls={} durationMs={} success={}",
                userId, toolCalls, durationMs, success);
    }
}
