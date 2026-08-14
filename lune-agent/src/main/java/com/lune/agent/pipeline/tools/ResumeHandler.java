package com.lune.agent.pipeline.tools;

import cn.hutool.json.JSONUtil;
import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 简历（工作经历 + 项目）工具处理器。 */
@Component
public class ResumeHandler implements ToolHandler {

    private final LuneApiClient api;

    public ResumeHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("create_work_experience", "update_work_experience", "delete_work_experience",
                "list_work_experiences", "create_project", "update_project", "delete_project", "list_projects");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        return switch (name) {
            case "create_work_experience" -> {
                var body = mapObj(a, "company", "position", "location", "description", "startDate", "endDate");
                body.set("isCurrent", a.getOrDefault("isCurrent", false));
                body.set("status", 1);
                var opt = api.createWorkExperience(body, token);
                if (opt.isPresent()) yield m("success", true, "message", "已创建", "preview", preview(opt.get()));
                yield m("success", false, "message", "创建失败");
            }
            case "update_work_experience" -> {
                long id = num(a, "id");
                var body = mapObj(a, "company", "position", "location", "description", "startDate", "endDate");
                body.set("isCurrent", a.getOrDefault("isCurrent", false));
                var opt = api.updateWorkExperience(id, body, token);
                if (opt.isPresent()) yield m("success", true, "message", "已更新", "preview", preview(opt.get()));
                yield m("success", false, "message", "更新失败");
            }
            case "delete_work_experience" -> deleted(api.deleteWorkExperience(num(a, "id"), token));
            case "list_work_experiences" -> {
                var opt = api.listWorkExperiences(token);
                if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                yield m("success", true, "message", "检索成功", "experiences", opt.get().get("records"));
            }
            case "create_project" -> {
                var body = mapObj(a, "name", "summary", "description", "role", "projectUrl");
                body.set("status", 1);
                if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
                var opt = api.createProject(body, token);
                if (opt.isPresent()) yield m("success", true, "message", "已创建", "preview", preview(opt.get()));
                yield m("success", false, "message", "创建失败");
            }
            case "update_project" -> {
                long id = num(a, "id");
                var body = mapObj(a, "name", "summary", "description", "role", "projectUrl");
                if (a.containsKey("techStack")) body.set("techStack", JSONUtil.toJsonStr(a.get("techStack")));
                var opt = api.updateProject(id, body, token);
                if (opt.isPresent()) yield m("success", true, "message", "已更新", "preview", preview(opt.get()));
                yield m("success", false, "message", "更新失败");
            }
            case "delete_project" -> deleted(api.deleteProject(num(a, "id"), token));
            case "list_projects" -> {
                var opt = api.listProjects(token);
                if (opt.isEmpty()) yield m("success", false, "message", "查询失败");
                yield m("success", true, "message", "检索成功", "projects", opt.get().get("records"));
            }
            default -> m("success", false, "message", "Unknown resume tool: " + name);
        };
    }
}
