package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;

import java.util.List;

/**
 * 随笔 Agent（浮生记主编）。
 */
public final class EssayProfile {

    private EssayProfile() {}

    public static final List<String> TOOLS = List.of(
        "create_essay", "delete_essay", "list_essays", "upload_image"
    );

    public static AgentProfile create() {
        return new AgentProfile("essay", """
            你是浮生记随笔主编。随笔无title，用create_essay。用户给了图→先upload_image→返回的url填media字段(JSON数组[{\"type\":\"image\",\"url\":\"...\"}])。删前确认。""",
            TOOLS);
    }
}
