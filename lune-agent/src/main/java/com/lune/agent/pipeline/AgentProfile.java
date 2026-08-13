package com.lune.agent.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 领域 Agent 配置档案。
 *
 * <p>每个领域 Agent 有独立的 system prompt（极简）、工具白名单、路由关键词与显式前缀。
 * 不引入独立进程——复用同一个 AgentOrchestrator + ToolExecutor。
 * 风格偏好不写死在 prompt 里，由 {@link #buildPrompt(Map)} 从 UserPreference 动态注入。</p>
 *
 * @param name         领域名，如 article / essay / record / work / project / general
 * @param prefix       显式路由前缀，如 "@文章"（general 为 null）
 * @param systemPrompt 极简系统提示词
 * @param toolNames    该领域可用的工具名白名单
 * @param keywords     关键词路由（小写匹配）
 */
public record AgentProfile(
    String name,
    String prefix,
    String systemPrompt,
    List<String> toolNames,
    List<String> keywords
) {
    /** 关键词是否命中（小写文本）。 */
    public boolean matches(String lower) {
        for (var kw : keywords) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    /**
     * 拼接偏好摘要到 system prompt 末尾（一句简短备注，不指定具体风格）。
     * 仅拼接该领域关心的偏好，无则原样返回。
     */
    public String buildPrompt(Map<String, String> prefs) {
        if (prefs == null || prefs.isEmpty()) return systemPrompt;
        var parts = new ArrayList<String>();
        switch (name) {
            case "article" -> {
                if (has(prefs, "article_style")) parts.add("风格=" + prefs.get("article_style"));
                if ("false".equalsIgnoreCase(prefs.get("article_auto_title"))) parts.add("自动拟标题已关闭，用户未给标题时先询问");
            }
            case "essay" -> {
                if (has(prefs, "essay_style")) parts.add("风格=" + prefs.get("essay_style"));
                if (has(prefs, "essay_default_location")) parts.add("常驻=" + prefs.get("essay_default_location"));
            }
            case "record" -> {
                if (has(prefs, "record_style")) parts.add("风格=" + prefs.get("record_style"));
            }
            case "work" -> {
                if (has(prefs, "work_template")) parts.add("模板=" + prefs.get("work_template"));
            }
            case "project" -> {
                if (has(prefs, "project_template")) parts.add("模板=" + prefs.get("project_template"));
            }
        }
        return parts.isEmpty() ? systemPrompt : systemPrompt + "\n当前设置：" + String.join("，", parts) + "。";
    }

    private static boolean has(Map<String, String> prefs, String key) {
        var v = prefs.get(key);
        return v != null && !v.isBlank();
    }
}
