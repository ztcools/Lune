package com.lune.controller;

import com.lune.common.Result;
import com.lune.service.SiteConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/site-config")
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    public SiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping("/public")
    public Result<Map<String, String>> getPublic() {
        return Result.success(siteConfigService.getPublicConfigs());
    }
}
