package com.lune.controller;

import com.lune.common.Result;
import com.lune.service.ProjectService;
import com.lune.service.WorkExperienceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 简历页公开数据：工作经历 + 项目经历
 */
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final WorkExperienceService workExperienceService;
    private final ProjectService projectService;

    public ResumeController(WorkExperienceService workExperienceService, ProjectService projectService) {
        this.workExperienceService = workExperienceService;
        this.projectService = projectService;
    }

    @GetMapping
    public Result<Map<String, Object>> getResume() {
        return Result.success(Map.of(
                "workExperiences", workExperienceService.listPublic(),
                "projects", projectService.listPublic()
        ));
    }
}
