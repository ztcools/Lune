package com.lune.agent.pipeline;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 集中管理所有 Agent 工具定义（OpenAI Function Calling 格式）。
 *
 * <p>用声明式 builder 构建，避免手写 JSON 字符串的转义/漏逗号问题；
 * 全量定义在类加载时解析一次并缓存，{@link #filterByNames} 仅按白名单取用，
 * 不再每次请求重复 parse 28 个定义。</p>
 *
 * <p><b>约定</b>：create_article 的 title 必填（Agent 自动拟标题），
 * create_essay/create_record/create_work_experience/create_project 无 title 字段。</p>
 */
public final class ToolDefinitions {

    private ToolDefinitions() {}

    /** 工具名 → 定义，插入顺序即全量顺序（稳定，便于 diff/日志）。 */
    private static final Map<String, JSONObject> DEFS = new LinkedHashMap<>();

    static {
        // ── 文章 ──
        def("create_article", "创建文章草稿。标题必填（用户未提供则自拟），正文为 HTML。建完告知用户 id。",
            params(Prop.req("title", "string", "文章标题，用户未提供时自拟"),
                   Prop.req("content", "string", "HTML 正文"),
                   Prop.p("summary", "string"), Prop.p("categoryId", "integer"), Prop.p("cover", "string")));
        def("publish_article", "发布指定 id 的文章。title+content 从 create_article 结果获取。",
            params(Prop.req("id", "integer"), Prop.req("title", "string"), Prop.req("content", "string")));
        def("update_article", "更新文章（仅传需要修改的字段）",
            params(Prop.req("id", "integer"), Prop.p("title", "string"), Prop.p("content", "string"),
                   Prop.p("summary", "string"), Prop.p("categoryId", "integer"), Prop.p("cover", "string")));
        def("delete_article", "删除文章，先确认",
            params(Prop.req("id", "integer")));
        def("list_articles", "查文章列表",
            params(Prop.p("page", "integer"), Prop.p("size", "integer")));

        // ── 随笔 ──
        def("create_essay", "创建随笔（朋友圈式动态，无标题）。content 必填。",
            params(Prop.req("content", "string", "随笔正文"),
                   Prop.p("mood", "string"), Prop.p("weather", "string"), Prop.p("location", "string"),
                   Prop.p("media", "string", "媒体 JSON 数组，如 [{\"type\":\"image\",\"url\":\"...\"}]")));
        def("delete_essay", "删除随笔",
            params(Prop.req("id", "integer")));
        def("list_essays", "查随笔列表",
            params(Prop.p("page", "integer"), Prop.p("size", "integer")));

        // ── 记录 ──
        def("create_record", "创建记录（打卡式）。title、content、categoryId 必填。",
            params(Prop.req("title", "string", "记录标题，用户未提供时自拟"),
                   Prop.req("content", "string", "记录正文"),
                   Prop.req("categoryId", "integer", "分类 ID（先 list_categories 获取，须为 record 类型）"),
                   Prop.p("cover", "string"), Prop.p("media", "string", "媒体 JSON 数组")));
        def("delete_record", "删除记录",
            params(Prop.req("id", "integer")));
        def("list_records", "查记录列表",
            params(Prop.p("page", "integer"), Prop.p("size", "integer"), Prop.p("categoryId", "integer")));

        // ── 树洞 ──
        def("list_treeholes", "查树洞列表",
            params(Prop.p("page", "integer"), Prop.p("size", "integer")));
        def("delete_treehole", "删树洞消息",
            params(Prop.req("id", "integer")));

        // ── 许愿池 ──
        def("list_wishes", "查许愿池",
            params(Prop.p("page", "integer"), Prop.p("size", "integer")));
        def("manage_wish", "管理许愿（目前仅支持删除）",
            params(Prop.req("id", "integer"), Prop.reqEnum("action", new String[]{"delete"})));

        // ── 网站配置 ──
        def("get_site_config", "获取所有网站配置",
            params());
        def("update_site_config", "改网站配置（key 如 site_name/site_logo/beian_icp 等）",
            params(Prop.req("configKey", "string"), Prop.req("configValue", "string")));

        // ── 分类 ──
        def("list_categories", "列出文章分类/记录分类",
            params());

        // ── 简历·工作经历 ──
        def("create_work_experience", "创建简历工作经历（无标题）。公司+职位+开始日期必填。",
            params(Prop.req("company", "string"), Prop.req("position", "string"), Prop.req("startDate", "string"),
                   Prop.p("endDate", "string"), Prop.p("isCurrent", "boolean"), Prop.p("description", "string")));
        def("update_work_experience", "更新工作经历",
            params(Prop.req("id", "integer"), Prop.p("company", "string"), Prop.p("position", "string"),
                   Prop.p("startDate", "string"), Prop.p("endDate", "string"), Prop.p("isCurrent", "boolean"),
                   Prop.p("description", "string")));
        def("delete_work_experience", "删工作经历",
            params(Prop.req("id", "integer")));
        def("list_work_experiences", "查所有工作经历",
            params());

        // ── 简历·项目 ──
        def("create_project", "创建简历项目（无标题）。项目名+简介必填。",
            params(Prop.req("name", "string"), Prop.req("summary", "string"), Prop.p("description", "string"),
                   Prop.pArray("techStack", "string"), Prop.p("role", "string"), Prop.p("projectUrl", "string")));
        def("update_project", "更新项目",
            params(Prop.req("id", "integer"), Prop.p("name", "string"), Prop.p("summary", "string"),
                   Prop.p("description", "string"), Prop.pArray("techStack", "string"), Prop.p("role", "string"),
                   Prop.p("projectUrl", "string")));
        def("delete_project", "删项目",
            params(Prop.req("id", "integer")));
        def("list_projects", "查所有项目",
            params());

        // ── 资源 & 统计 ──
        def("upload_image", "上传图片到资源库，返回可用的图片 URL。传入图片 URL，系统下载后存入存储。用于文章封面/随笔媒体/记录附图。",
            params(Prop.req("url", "string", "图片 URL")));
        def("get_dashboard_stats", "网站统计数据（文章/随笔/树洞/许愿总数）",
            params());
    }

    /** 全量定义（按注册顺序）。 */
    public static JSONArray allDefinitions() {
        var arr = new JSONArray();
        DEFS.values().forEach(arr::add);
        return arr;
    }

    /** 按工具名白名单筛选定义（保持注册顺序）。 */
    public static JSONArray filterByNames(List<String> names) {
        var filtered = new JSONArray();
        for (var name : names) {
            var d = DEFS.get(name);
            if (d != null) filtered.add(d);
        }
        return filtered;
    }

    /** 全部工具名（通用 Agent 用）。 */
    public static List<String> allNames() {
        return List.copyOf(DEFS.keySet());
    }

    // ── Builder helpers ──

    private static void def(String name, String description, JSONObject params) {
        var fn = new JSONObject();
        fn.set("name", name);
        fn.set("description", description);
        fn.set("parameters", params);
        var tool = new JSONObject();
        tool.set("type", "function");
        tool.set("function", fn);
        DEFS.put(name, tool);
    }

    private static JSONObject params(Prop... props) {
        var o = new JSONObject();
        o.set("type", "object");
        var properties = new JSONObject();
        var required = new JSONArray();
        for (var p : props) {
            var field = new JSONObject();
            field.set("type", p.type());
            if (p.desc() != null) field.set("description", p.desc());
            if (p.enums() != null) field.set("enum", new JSONArray(java.util.List.of(p.enums())));
            if (p.items() != null) field.set("items", new JSONObject().set("type", p.items()));
            properties.set(p.name(), field);
            if (p.required()) required.add(p.name());
        }
        o.set("properties", properties);
        if (!required.isEmpty()) o.set("required", required);
        return o;
    }

    /** 工具参数描述。 */
    private record Prop(String name, String type, String desc, boolean required, String[] enums, String items) {
        static Prop p(String name, String type) { return new Prop(name, type, null, false, null, null); }
        static Prop p(String name, String type, String desc) { return new Prop(name, type, desc, false, null, null); }
        static Prop req(String name, String type) { return new Prop(name, type, null, true, null, null); }
        static Prop req(String name, String type, String desc) { return new Prop(name, type, desc, true, null, null); }
        static Prop reqEnum(String name, String[] enums) { return new Prop(name, "string", null, true, enums, null); }
        static Prop pArray(String name, String items) { return new Prop(name, "array", null, false, null, items); }
    }
}
