package com.lune.agent.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AgentException} 基础契约测试。
 */
class AgentExceptionTest {

    @Test
    void messageOnly() {
        AgentException e = new AgentException("工具执行失败");
        assertThat(e.getMessage()).isEqualTo("工具执行失败");
        assertThat(e.getCause()).isNull();
    }

    @Test
    void messageAndCause() {
        RuntimeException cause = new RuntimeException("upstream");
        AgentException e = new AgentException("包装", cause);
        assertThat(e.getMessage()).isEqualTo("包装");
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void isRuntimeException() {
        assertThat(new AgentException("x")).isInstanceOf(RuntimeException.class);
    }
}
