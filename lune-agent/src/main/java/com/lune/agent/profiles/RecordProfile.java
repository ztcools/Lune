package com.lune.agent.profiles;

import com.lune.agent.pipeline.AgentProfile;

import java.util.List;

/**
 * 记录 Agent（光阴集主编）。
 */
public final class RecordProfile {

    private RecordProfile() {}

    public static final List<String> TOOLS = List.of(
        "create_record", "delete_record", "list_records", "list_categories", "upload_image"
    );

    public static AgentProfile create() {
        return new AgentProfile("record", """
            你是光阴集记录主编。categoryId必填→先list_categories自选。用户给了图→先upload_image。删前确认。""",
            TOOLS);
    }
}
