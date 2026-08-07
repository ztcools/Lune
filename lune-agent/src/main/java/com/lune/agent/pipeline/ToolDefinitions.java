package com.lune.agent.pipeline;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * 集中管理所有 Agent 工具定义。
 *
 * <p>从 ToolExecutor.getDefinitions() 移出，方便各领域 Profile 按白名单筛选工具。
 * 每个工具定义保持与原 ToolExecutor 中完全一致。</p>
 *
 * <p><b>注意</b>：create_article 的 title 为必填（Agent 会自动拟标题），
 * create_essay/create_record/create_work_experience/create_project 无 title 字段。</p>
 */
public final class ToolDefinitions {

    private ToolDefinitions() {}

    public static final String CREATE_ARTICLE = """
        {"type":"function","function":{"name":"create_article","description":"创建文章草稿。需要标题+内容，可选摘要/分类/封面。建完告诉用户id。","parameters":{"type":"object","properties":{"title":{"type":"string","description":"文章标题，如用户未提供请根据内容自动生成"},"content":{"type":"string","description":"HTML格式正文"},"summary":{"type":"string"},"categoryId":{"type":"integer"},"cover":{"type":"string"}},"required":["title","content"]}}}""";

    public static final String PUBLISH_ARTICLE = """
        {"type":"function","function":{"name":"publish_article","description":"发布指定id的文章。必须传入title+content（从create_article结果中获取）。","parameters":{"type":"object","properties":{"id":{"type":"integer"},"title":{"type":"string"},"content":{"type":"string"}},"required":["id","title","content"]}}}""";

    public static final String UPDATE_ARTICLE = """
        {"type":"function","function":{"name":"update_article","description":"更新文章","parameters":{"type":"object","properties":{"id":{"type":"integer"},"title":{"type":"string"},"content":{"type":"string"},"summary":{"type":"string"},"categoryId":{"type":"integer"},"cover":{"type":"string"}},"required":["id"]}}}""";

    public static final String DELETE_ARTICLE = """
        {"type":"function","function":{"name":"delete_article","description":"删除文章，先确认","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}}""";

    public static final String LIST_ARTICLES = """
        {"type":"function","function":{"name":"list_articles","description":"查文章列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}}""";

    public static final String CREATE_ESSAY = """
        {"type":"function","function":{"name":"create_essay","description":"创建随笔(朋友圈式动态，无标题)。content必填，可选心情/天气/位置/媒体。","parameters":{"type":"object","properties":{"content":{"type":"string","description":"随笔正文"},"mood":{"type":"string"},"weather":{"type":"string"},"location":{"type":"string"},"media":{"type":"string","description":"媒体JSON数组"}},"required":["content"]}}}""";

    public static final String DELETE_ESSAY = """
        {"type":"function","function":{"name":"delete_essay","description":"删除随笔","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}}""";

    public static final String LIST_ESSAYS = """
        {"type":"function","function":{"name":"list_essays","description":"查随笔列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}}""";

    public static final String CREATE_RECORD = """
        {"type":"function","function":{"name":"create_record","description":"创建记录(打卡式，无标题)。content必填，categoryId必填。","parameters":{"type":"object","properties":{"content":{"type":"string","description":"记录正文"},"categoryId":{"type":"integer","description":"分类ID(必填)"},"cover":{"type":"string"},"media":{"type":"string","description":"媒体JSON数组"}},"required":["content","categoryId"]}}}""";

    public static final String DELETE_RECORD = """
        {"type":"function","function":{"name":"delete_record","description":"删除记录","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}}""";

    public static final String LIST_RECORDS = """
        {"type":"function","function":{"name":"list_records","description":"查记录列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"},"categoryId":{"type":"integer"}}}}}""";

    public static final String LIST_TREEHOLES = """
        {"type":"function","function":{"name":"list_treeholes","description":"查树洞列表","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}}""";

    public static final String DELETE_TREEHOLE = """
        {"type":"function","function":{"name":"delete_treehole","description":"删树洞消息","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}}""";

    public static final String LIST_WISHES = """
        {"type":"function","function":{"name":"list_wishes","description":"查许愿池","parameters":{"type":"object","properties":{"page":{"type":"integer"},"size":{"type":"integer"}}}}}""";

    public static final String MANAGE_WISH = """
        {"type":"function","function":{"name":"manage_wish","description":"管理许愿(删除)","parameters":{"type":"object","properties":{"id":{"type":"integer"},"action":{"type":"string","enum":["delete"]}},"required":["id","action"]}}}""";

    public static final String GET_SITE_CONFIG = """
        {"type":"function","function":{"name":"get_site_config","description":"获取所有网站配置","parameters":{"type":"object","properties":{}}}}""";

    public static final String UPDATE_SITE_CONFIG = """
        {"type":"function","function":{"name":"update_site_config","description":"改网站配置(key如site_name/site_logo/beian_icp等)","parameters":{"type":"object","properties":{"configKey":{"type":"string"},"configValue":{"type":"string"}},"required":["configKey","configValue"]}}}""";

    public static final String LIST_CATEGORIES = """
        {"type":"function","function":{"name":"list_categories","description":"列出文章分类/记录分类","parameters":{"type":"object","properties":{}}}}""";

    public static final String CREATE_WORK_EXPERIENCE = """
        {"type":"function","function":{"name":"create_work_experience","description":"创建简历工作经历（无标题）。需公司+职位+开始日期。","parameters":{"type":"object","properties":{"company":{"type":"string"},"position":{"type":"string"},"startDate":{"type":"string"},"endDate":{"type":"string"},"isCurrent":{"type":"boolean"},"description":{"type":"string"}},"required":["company","position","startDate"]}}}""";

    public static final String UPDATE_WORK_EXPERIENCE = """
        {"type":"function","function":{"name":"update_work_experience","description":"更新工作经历","parameters":{"type":"object","properties":{"id":{"type":"integer"},"company":{"type":"string"},"position":{"type":"string"},"startDate":{"type":"string"},"endDate":{"type":"string"},"isCurrent":{"type":"boolean"},"description":{"type":"string"}},"required":["id"]}}}""";

    public static final String DELETE_WORK_EXPERIENCE = """
        {"type":"function","function":{"name":"delete_work_experience","description":"删工作经历","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}}""";

    public static final String LIST_WORK_EXPERIENCES = """
        {"type":"function","function":{"name":"list_work_experiences","description":"查所有工作经历","parameters":{"type":"object","properties":{}}}}""";

    public static final String CREATE_PROJECT = """
        {"type":"function","function":{"name":"create_project","description":"创建简历项目（无标题）。需项目名+简介。","parameters":{"type":"object","properties":{"name":{"type":"string"},"summary":{"type":"string"},"description":{"type":"string"},"techStack":{"type":"array","items":{"type":"string"}},"role":{"type":"string"},"projectUrl":{"type":"string"}},"required":["name","summary"]}}}""";

    public static final String UPDATE_PROJECT = """
        {"type":"function","function":{"name":"update_project","description":"更新项目","parameters":{"type":"object","properties":{"id":{"type":"integer"},"name":{"type":"string"},"summary":{"type":"string"},"description":{"type":"string"},"techStack":{"type":"array","items":{"type":"string"}},"role":{"type":"string"},"projectUrl":{"type":"string"}},"required":["id"]}}}""";

    public static final String DELETE_PROJECT = """
        {"type":"function","function":{"name":"delete_project","description":"删项目","parameters":{"type":"object","properties":{"id":{"type":"integer"}},"required":["id"]}}}""";

    public static final String LIST_PROJECTS = """
        {"type":"function","function":{"name":"list_projects","description":"查所有项目","parameters":{"type":"object","properties":{}}}}""";

    public static final String UPLOAD_IMAGE = """
        {"type":"function","function":{"name":"upload_image","description":"上传图片到资源库，返回CDN URL。传入图片URL，系统下载后存入COS。用于文章封面/随笔媒体/记录附图。","parameters":{"type":"object","properties":{"url":{"type":"string","description":"图片URL"}},"required":["url"]}}}""";

    public static final String GET_DASHBOARD_STATS = """
        {"type":"function","function":{"name":"get_dashboard_stats","description":"网站统计数据(文章/随笔/记录/树洞/许愿总数)","parameters":{"type":"object","properties":{}}}}""";

    public static JSONArray allDefinitions() {
        var arr = new JSONArray();
        arr.add(JSONUtil.parseObj(CREATE_ARTICLE));
        arr.add(JSONUtil.parseObj(PUBLISH_ARTICLE));
        arr.add(JSONUtil.parseObj(UPDATE_ARTICLE));
        arr.add(JSONUtil.parseObj(DELETE_ARTICLE));
        arr.add(JSONUtil.parseObj(LIST_ARTICLES));
        arr.add(JSONUtil.parseObj(CREATE_ESSAY));
        arr.add(JSONUtil.parseObj(DELETE_ESSAY));
        arr.add(JSONUtil.parseObj(LIST_ESSAYS));
        arr.add(JSONUtil.parseObj(CREATE_RECORD));
        arr.add(JSONUtil.parseObj(DELETE_RECORD));
        arr.add(JSONUtil.parseObj(LIST_RECORDS));
        arr.add(JSONUtil.parseObj(LIST_TREEHOLES));
        arr.add(JSONUtil.parseObj(DELETE_TREEHOLE));
        arr.add(JSONUtil.parseObj(LIST_WISHES));
        arr.add(JSONUtil.parseObj(MANAGE_WISH));
        arr.add(JSONUtil.parseObj(GET_SITE_CONFIG));
        arr.add(JSONUtil.parseObj(UPDATE_SITE_CONFIG));
        arr.add(JSONUtil.parseObj(LIST_CATEGORIES));
        arr.add(JSONUtil.parseObj(CREATE_WORK_EXPERIENCE));
        arr.add(JSONUtil.parseObj(UPDATE_WORK_EXPERIENCE));
        arr.add(JSONUtil.parseObj(DELETE_WORK_EXPERIENCE));
        arr.add(JSONUtil.parseObj(LIST_WORK_EXPERIENCES));
        arr.add(JSONUtil.parseObj(CREATE_PROJECT));
        arr.add(JSONUtil.parseObj(UPDATE_PROJECT));
        arr.add(JSONUtil.parseObj(DELETE_PROJECT));
        arr.add(JSONUtil.parseObj(LIST_PROJECTS));
        arr.add(JSONUtil.parseObj(UPLOAD_IMAGE));
        arr.add(JSONUtil.parseObj(GET_DASHBOARD_STATS));
        return arr;
    }

    /**
     * 根据工具名白名单筛选定义
     */
    public static JSONArray filterByNames(java.util.List<String> names) {
        var all = allDefinitions();
        var filtered = new JSONArray();
        for (int i = 0; i < all.size(); i++) {
            var tool = all.getJSONObject(i);
            String name = tool.getByPath("function.name", String.class);
            if (names.contains(name)) {
                filtered.add(tool);
            }
        }
        return filtered;
    }

    /** 根据工具名获取单条定义 */
    static JSONObject getByName(String name) {
        var all = allDefinitions();
        for (int i = 0; i < all.size(); i++) {
            var tool = all.getJSONObject(i);
            if (name.equals(tool.getByPath("function.name", String.class))) {
                return tool;
            }
        }
        return null;
    }
}
