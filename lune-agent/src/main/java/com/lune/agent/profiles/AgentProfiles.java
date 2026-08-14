package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;
import com.lune.agent.pipeline.ToolDefinitions;

import java.util.List;

/**
 * 领域 Agent 注册表 —— 数据驱动的意图路由。
 *
 * <p>6 个领域 Agent + 1 个通用兜底，全部在此声明。新增领域只需加一条常量
 * （prefix + prompt + 工具白名单 + 关键词），无需改动 AgentOrchestrator。</p>
 *
 * <p>路由规则：
 * <ol>
 *   <li>消息以 {@code @文章/@随笔/...} 开头 → 显式命中该领域；若剩余文本提到其它领域则退回通用。</li>
 *   <li>否则按关键词匹配：恰好命中一个领域 → 该领域；零个/多个 → 通用兜底。</li>
 * </ol>
 * 纯关键词匹配，零 LLM 调用。</p>
 */
public final class AgentProfiles {

    private AgentProfiles() {}

    // ── 领域 Agent ──

    public static final AgentProfile ARTICLE = new AgentProfile(
        "article", "@文章",
        "你是「云栖阁」文章主编。用户发来的内容就是文章素材，直接调用 create_article 写成草稿：标题用户没给就自拟；分类先 list_categories 选一个 article 类型；给了图先 upload_image 再填 cover。删改前先确认。",
        List.of("create_article", "publish_article", "update_article", "delete_article",
                "list_articles", "list_categories", "upload_image"),
        List.of("文章", "发文", "博客", "发布文章", "写篇"));

    public static final AgentProfile ESSAY = new AgentProfile(
        "essay", "@随笔",
        "你是「浮生记」随笔主编。用户发来的内容就是随笔正文，直接调用 create_essay 发布（随笔无标题，content 填正文）。给了图先 upload_image，把返回 url 填进 media 字段（格式 [{\"type\":\"image\",\"url\":\"...\"}]）。删前确认。",
        List.of("create_essay", "delete_essay", "list_essays", "upload_image"),
        List.of("随笔", "朋友圈", "动态", "心情", "浮生记"));

    public static final AgentProfile RECORD = new AgentProfile(
        "record", "@记录",
        "你是「光阴集」记录主编。用户发来的打卡/收藏内容就是一条记录，直接调用 create_record 发布：标题用户没给就自拟；分类先 list_categories 选一个 record 类型（categoryId 必填）。给了图先 upload_image。删前确认。",
        List.of("create_record", "delete_record", "list_records", "list_categories", "upload_image"),
        List.of("记录", "收藏", "打卡", "光阴集"));

    public static final AgentProfile WORK = new AgentProfile(
        "work", "@工作",
        "你是「山海志」履痕主编。用户描述的工作/实习经历，直接调用 create_work_experience 创建（公司+职位+开始日期必填）。删前确认。",
        List.of("create_work_experience", "update_work_experience",
                "delete_work_experience", "list_work_experiences"),
        List.of("工作", "实习", "履痕", "公司", "上班"));

    public static final AgentProfile PROJECT = new AgentProfile(
        "project", "@项目",
        "你是「山海志」造物集主编。用户描述的项目，直接调用 create_project 创建（项目名+简介必填）。删前确认。",
        List.of("create_project", "update_project", "delete_project", "list_projects"),
        List.of("项目", "造物集", "开源", "github"));

    public static final AgentProfile GENERAL = new AgentProfile(
        "general", null,
        "你是「Luna」站长助手。识别用户意图并调用对应工具：写文章→create_article（title 必填，用户没给就自拟；分类先 list_categories；图先 upload_image 再填 cover）；发随笔/心情→create_essay（无 title，图先 upload_image 再填 media）；打卡/记录→create_record（title+categoryId 必填，分类选 record 类型）；添工作经历→create_work_experience；添项目→create_project。删改前确认。",
        ToolDefinitions.allNames(),
        List.of());

    private static final List<AgentProfile> DOMAINS = List.of(ARTICLE, ESSAY, RECORD, WORK, PROJECT);

    /** 路由结果：命中的 profile + 去除前缀后的用户消息。 */
    public record Route(AgentProfile profile, String message) {}

    /**
     * 意图路由（前缀 → 关键词 → 通用）。
     */
    public static Route route(String raw) {
        if (raw == null) return new Route(GENERAL, raw);
        var msg = raw.trim();

        for (var p : DOMAINS) {
            if (msg.startsWith(p.prefix())) {
                var rest = msg.substring(p.prefix().length()).trim();
                var lower = rest.toLowerCase();
                boolean mentionsOther = DOMAINS.stream().anyMatch(d -> d != p && d.matches(lower));
                return new Route(mentionsOther ? GENERAL : p, rest);
            }
        }
        return new Route(routeByKeyword(msg), msg);
    }

    private static AgentProfile routeByKeyword(String msg) {
        if (msg.isBlank()) return GENERAL;
        var lower = msg.toLowerCase();
        var hits = DOMAINS.stream().filter(p -> p.matches(lower)).toList();
        return hits.size() == 1 ? hits.get(0) : GENERAL;
    }
}
