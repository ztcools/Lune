package com.lune.security;

/**
 * XSS 输入清洗（服务端兜底）。
 *
 * <p>前端渲染富文本时已有一层过滤，这里对"纯文本"用户输入
 * （评论、树洞、昵称、许愿等）做 HTML 特殊字符转义，防止存储型 XSS。
 * 注意：文章正文是 Markdown，不应转义（交给前端渲染层白名单过滤）。</p>
 */
public final class XssSanitizer {

    private XssSanitizer() {}

    /**
     * 转义 HTML 特殊字符，用于纯文本字段。
     * 返回 null 当输入为 null。
     */
    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 清洗并限制长度，返回安全文本。
     */
    public static String clean(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.length() > maxLength) {
            trimmed = trimmed.substring(0, maxLength);
        }
        return escape(trimmed);
    }
}
