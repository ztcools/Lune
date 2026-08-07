package com.lune.agent.common;

import java.util.Optional;

/**
 * 统一结果封装 —— 消除 null 返回模式，与 lune-web 的 Result 对齐。
 *
 * <p>失败时通过 {@link #error()} / {@link #error(String)} 创建，调用方无需判空。
 * 成功时通过 {@link #success(Object)} 创建并承载实际数据。</p>
 *
 * @param <T> 成功时的数据类型
 */
public record AgentResult<T>(boolean success, T data, String message) {

    /** 快速构建成功结果 */
    public static <T> AgentResult<T> success(T data) {
        return new AgentResult<>(true, data, null);
    }

    /** 构建成功结果并附带提示消息 */
    public static <T> AgentResult<T> success(T data, String message) {
        return new AgentResult<>(true, data, message);
    }

    /** 构建失败结果 */
    public static <T> AgentResult<T> error(String message) {
        return new AgentResult<>(false, null, message);
    }

    /** 转换为 Optional（空结果 → Optional.empty()） */
    public Optional<T> toOptional() {
        return success && data != null ? Optional.of(data) : Optional.empty();
    }
}
