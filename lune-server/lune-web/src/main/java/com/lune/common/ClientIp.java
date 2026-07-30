package com.lune.common;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 统一的客户端真实 IP 解析。
 *
 * <p>访问统计与限流必须用同一套解析规则：两处若不一致，
 * 就会出现「统计里是一个 IP、限流桶里是另一个 IP」的错位。
 *
 * <p><b>为什么优先 {@code X-Real-IP} 而不是 {@code X-Forwarded-For}：</b>
 * nginx 的 {@code $proxy_add_x_forwarded_for} 是<b>追加</b>语义。客户端自带
 * {@code X-Forwarded-For: 1.2.3.4} 时，后端收到的是 {@code "1.2.3.4, <真实IP>"}，
 * 取第一段拿到的就是客户端伪造值。{@code X-Real-IP} 由 nginx 以
 * {@code $remote_addr} 覆写，客户端无法伪造。
 *
 * <p>回退到 XFF 时取<b>最后</b>一段（最靠近本服务的代理写入的那个），而非第一段。
 * 若取第一段，攻击者只要每次请求换一个 XFF 值就能拿到全新的限流桶，
 * 后端所有按 IP 的限流（含 auth 5r/m 的登录爆破防护）全部失效。
 */
public final class ClientIp {

    /** visit_log.ip 与 Redis key 的长度上限（IPv6 全展开 45 字符 + 余量） */
    private static final int MAX_LEN = 50;

    private ClientIp() {}

    public static String resolve(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (isUsable(realIp)) return truncate(realIp.trim());

        String xff = request.getHeader("X-Forwarded-For");
        if (isUsable(xff)) {
            int idx = xff.lastIndexOf(',');
            String last = idx >= 0 ? xff.substring(idx + 1) : xff;
            if (isUsable(last)) return truncate(last.trim());
        }
        return truncate(request.getRemoteAddr());
    }

    private static boolean isUsable(String v) {
        return v != null && !v.isBlank() && !"unknown".equalsIgnoreCase(v.trim());
    }

    /**
     * 截断到列宽。无 nginx 的本地直连场景下 XFF 完全由客户端控制，
     * 超长值会让 insert 直接抛 Data truncation，这里兜住。
     */
    private static String truncate(String ip) {
        if (ip == null) return null;
        return ip.length() <= MAX_LEN ? ip : ip.substring(0, MAX_LEN);
    }
}
