package com.lune.agent.pipeline;

import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 工具处理器共享辅助方法。
 */
public final class ToolSupport {

    private ToolSupport() {}

    /** 快捷构造 Map：{@code m("success", true, "message", "ok")}。 */
    public static Map<String, Object> m(Object... kv) {
        var r = new HashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    /** 读取数值型参数为 long，缺失或非数值返回 0（防御 NPE/ClassCastException）。 */
    public static long num(Map<String, Object> m, String k) {
        var v = m.get(k);
        return v instanceof Number n ? n.longValue() : 0L;
    }

    /** 分页参数（默认第 1 页）。 */
    public static int page(Map<String, Object> a) {
        return a.get("page") instanceof Number n ? n.intValue() : 1;
    }

    /** 分页大小（默认 10）。 */
    public static int size(Map<String, Object> a) {
        return a.get("size") instanceof Number n ? n.intValue() : 10;
    }

    /** 从 src 中挑指定 key 构造新 JSONObject（跳过缺失/null）。 */
    public static JSONObject mapObj(Map<String, Object> src, String... keys) {
        var dst = new JSONObject();
        copy(src, dst, keys);
        return dst;
    }

    /** 从 src 复制指定 key 到 dst（跳过缺失/null）。 */
    public static void copy(Map<String, Object> src, JSONObject dst, String... keys) {
        for (var k : keys) if (src.containsKey(k) && src.get(k) != null) dst.set(k, src.get(k));
    }

    /** 将 JSONObject 平铺为 Map（供前端 preview 展示）。 */
    public static Map<String, Object> preview(JSONObject obj) {
        var m = new LinkedHashMap<String, Object>();
        for (var k : obj.keySet()) m.put(k, obj.get(k));
        return m;
    }

    /** 删除类工具的统一返回：根据 API 删除结果上报成败。 */
    public static Map<String, Object> deleted(boolean ok) {
        return m("success", ok, "message", ok ? "已删除" : "删除失败");
    }

    /** 列表类工具的统一返回：共 N 条 + total + records。 */
    public static Map<String, Object> listResult(Optional<JSONObject> opt, String unit, String key) {
        if (opt.isEmpty()) return m("success", false, "message", "查询失败");
        var obj = opt.get();
        return m("success", true, "message", "共 " + obj.get("total") + unit,
                "total", obj.get("total"), key, obj.get("records"));
    }
}
