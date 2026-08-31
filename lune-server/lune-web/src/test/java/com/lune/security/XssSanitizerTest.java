package com.lune.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XssSanitizer} 纯文本字段 XSS 转义测试。
 */
class XssSanitizerTest {

    @Test
    void escapeNullReturnsNull() {
        assertThat(XssSanitizer.escape(null)).isNull();
    }

    @Test
    void escapeHtmlSpecialChars() {
        assertThat(XssSanitizer.escape("<script>alert('x')</script>"))
                .isEqualTo("&lt;script&gt;alert(&#x27;x&#x27;)&lt;/script&gt;");
    }

    @Test
    void escapeAmpersandAndQuotes() {
        assertThat(XssSanitizer.escape("a & b \"c\" d"))
                .isEqualTo("a &amp; b &quot;c&quot; d");
    }

    @Test
    void escapePlainTextUnchanged() {
        assertThat(XssSanitizer.escape("普通文本 hello 123")).isEqualTo("普通文本 hello 123");
    }

    @Test
    void cleanTrimsAndLimitsLength() {
        // trim 后 "<b>x</b>" 截断到 3 个字符 "<b>"，再转义
        assertThat(XssSanitizer.clean("  <b>x</b>  ", 3))
                .isEqualTo("&lt;b&gt;");
    }

    @Test
    void cleanNullReturnsNull() {
        assertThat(XssSanitizer.clean(null, 10)).isNull();
    }
}
