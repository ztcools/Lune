package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;

import java.util.List;

/**
 * 文章 Agent（云栖阁主编）。
 */
public final class ArticleProfile {

    private ArticleProfile() {}

    public static final List<String> TOOLS = List.of(
        "create_article", "publish_article", "update_article", "delete_article",
        "list_articles", "list_categories", "upload_image"
    );

    public static AgentProfile create() {
        return new AgentProfile("article", """
            你是云栖阁文章主编。用户给了图→先upload_image再填cover。用户没说标题→你拟。分类先list_categories后自选。删前确认。""",
            TOOLS);
    }
}
