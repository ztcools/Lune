package com.lune.controller.admin;

import com.lune.common.Result;
import com.lune.entity.Project;
import com.lune.entity.WorkExperience;
import com.lune.service.ProjectService;
import com.lune.service.WorkExperienceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/resume")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResumeController {

    private final WorkExperienceService workExperienceService;
    private final ProjectService projectService;

    public AdminResumeController(WorkExperienceService workExperienceService, ProjectService projectService) {
        this.workExperienceService = workExperienceService;
        this.projectService = projectService;
    }

    // ===== 工作经历 =====
    @GetMapping("/work")
    public Result<List<WorkExperience>> listWork() {
        return Result.success(workExperienceService.listAll());
    }

    @PostMapping("/work")
    public Result<WorkExperience> createWork(@RequestBody WorkExperience item) {
        return Result.success(workExperienceService.create(item));
    }

    @PutMapping("/work/{id}")
    public Result<WorkExperience> updateWork(@PathVariable Long id, @RequestBody WorkExperience item) {
        return Result.success(workExperienceService.update(id, item));
    }

    @DeleteMapping("/work/{id}")
    public Result<Void> deleteWork(@PathVariable Long id) {
        workExperienceService.delete(id);
        return Result.success();
    }

    // ===== 项目经历 =====
    @GetMapping("/project")
    public Result<List<Project>> listProject() {
        return Result.success(projectService.listAll());
    }

    @PostMapping("/project")
    public Result<Project> createProject(@RequestBody Project item) {
        return Result.success(projectService.create(item));
    }

    @PutMapping("/project/{id}")
    public Result<Project> updateProject(@PathVariable Long id, @RequestBody Project item) {
        return Result.success(projectService.update(id, item));
    }

    @DeleteMapping("/project/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }
}
