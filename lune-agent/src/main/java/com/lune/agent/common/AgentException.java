package com.lune.agent.common;

/**
 * Agent 异常体系根类。
 *
 * <p>所有 Agent 层的异常均继承此类，调用方可以统一捕获。
 * 区分于 Spring 框架异常和 JDK 异常。</p>
 */
public class AgentException extends RuntimeException {

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
