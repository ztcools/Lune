package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;

import java.util.List;

/**
 * 项目 Agent（山海志·造物集）。
 */
public final class ProjectProfile {

    private ProjectProfile() {}

    public static final List<String> TOOLS = List.of(
        "create_project", "update_project", "delete_project", "list_projects"
    );

    public static AgentProfile create() {
        return new AgentProfile("project", """
            你是山海志项目主编。用户添项目→调create_project。删前确认。""",
            TOOLS);
    }
}
