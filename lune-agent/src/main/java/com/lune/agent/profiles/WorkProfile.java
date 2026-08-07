package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;

import java.util.List;

/**
 * 工作经历 Agent（山海志·履痕）。
 */
public final class WorkProfile {

    private WorkProfile() {}

    public static final List<String> TOOLS = List.of(
        "create_work_experience", "update_work_experience",
        "delete_work_experience", "list_work_experiences"
    );

    public static AgentProfile create() {
        return new AgentProfile("work", """
            你是山海志工作主编。用户添经历→调create_work_experience。删前确认。""",
            TOOLS);
    }
}
