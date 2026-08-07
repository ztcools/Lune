package com.lune.agent.pipeline;

import java.util.List;
import java.util.Map;

/**
 * 领域 Agent 配置档案。
 *
 * <p>每个领域 Agent 有独立的 system prompt（极简）和工具白名单。
 * 不引入新进程——复用同一个 AgentOrchestrator + ToolExecutor。
 * 风格偏好不写在 prompt 里，由调用方从 UserPreference 读取后动态注入。</p>
 */
public record AgentProfile(
    /** 领域名，如 article / essay / record / work / project / general */
    String name,
    /** 极简系统提示词 */
    String systemPrompt,
    /** 该领域可用的工具名白名单 */
    List<String> toolNames
) {
    /**
     * 拼接偏好摘要到 system prompt 末尾（一句简短备注，不指定风格）。
     *
     * @param prefs 用户偏好 Map（来自 UserPreference）
     * @return 拼接后的完整 system prompt
     */
    public String buildPrompt(Map<String, String> prefs) {
        var sb = new StringBuilder(systemPrompt);
        if (prefs != null && !prefs.isEmpty()) {
            // 根据领域名只拼接相关偏好
            sb.append("\n当前设置：");
            switch (name) {
                case "article" -> {
                    if (prefs.containsKey("article_default_category"))
                        sb.append("默认分类=").append(prefs.get("article_default_category")).append("，");
                    if (prefs.containsKey("article_style"))
                        sb.append("偏好风格=").append(prefs.get("article_style")).append("，");
                }
                case "essay" -> {
                    if (prefs.containsKey("essay_default_location"))
                        sb.append("常驻=").append(prefs.get("essay_default_location")).append("，");
                }
                case "record" -> {
                    if (prefs.containsKey("record_default_category"))
                        sb.append("默认分类=").append(prefs.get("record_default_category")).append("，");
                }
            }
            // 去掉末尾逗号
            if (sb.charAt(sb.length() - 1) == '，') sb.setLength(sb.length() - 1);
            sb.append("。");
        }
        return sb.toString();
    }
}
