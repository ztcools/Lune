package com.lune.controller.admin;

import com.lune.common.Result;
import com.lune.entity.SiteConfig;
import com.lune.service.SiteConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/site-configs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    public AdminSiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping
    public Result<List<SiteConfig>> list() {
        return Result.success(siteConfigService.listAll());
    }

    @PostMapping
    public Result<SiteConfig> save(@RequestBody SiteConfig config) {
        return Result.success(siteConfigService.saveOrUpdate(config));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        siteConfigService.deleteConfig(id);
        return Result.success();
    }
}
