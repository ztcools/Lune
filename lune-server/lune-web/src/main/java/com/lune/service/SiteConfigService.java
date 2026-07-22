package com.lune.service;

import com.lune.entity.SiteConfig;
import java.util.List;
import java.util.Map;

public interface SiteConfigService {
    Map<String, String> getPublicConfigs();
    List<SiteConfig> listAll();
    SiteConfig saveOrUpdate(SiteConfig config);
    void deleteConfig(Long id);
}
