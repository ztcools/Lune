package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;
import com.lune.agent.pipeline.ToolDefinitions;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Agent（兜底）。
 */
public final class GeneralProfile {

    private GeneralProfile() {}

    public static AgentProfile create() {
        var allDefs = ToolDefinitions.allDefinitions();
        var allNames = new ArrayList<String>();
        for (int i = 0; i < allDefs.size(); i++) {
            allNames.add(allDefs.getJSONObject(i).getByPath("function.name", String.class));
        }
        return new AgentProfile("general", """
            你是Luna站长助手。-文章:create_article(title必填→用户没给就拟,先list_categories选分类,图→upload_image→填cover)-随笔:create_essay(无title,图→upload_image→media用[{type:image,url:xxx}]格式)-记录:create_record(categoryId必填)-删前确认""",
            List.copyOf(allNames));
    }
}
