package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.entity.SiteConfig;
import com.lune.mapper.SiteConfigMapper;
import com.lune.service.SiteConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiteConfigServiceImpl implements SiteConfigService {

    private final SiteConfigMapper siteConfigMapper;

    public SiteConfigServiceImpl(SiteConfigMapper siteConfigMapper) {
        this.siteConfigMapper = siteConfigMapper;
    }

    @Override
    @Cacheable(value = "siteConfig", key = "'public'")
    public Map<String, String> getPublicConfigs() {
        var configs = siteConfigMapper.selectList(
            new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigType, "public")
        );
        var map = new LinkedHashMap<String, String>();
        for (var c : configs) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    @Override
    public List<SiteConfig> listAll() {
        return siteConfigMapper.selectList(null);
    }

    @Override
    @CacheEvict(value = "siteConfig", key = "'public'")
    public SiteConfig saveOrUpdate(SiteConfig config) {
        var exist = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigKey, config.getConfigKey()));
        if (exist != null) {
            exist.setConfigValue(config.getConfigValue());
            exist.setDescription(config.getDescription());
            siteConfigMapper.updateById(exist);
            return exist;
        }
        siteConfigMapper.insert(config);
        return config;
    }

    @Override
    @CacheEvict(value = "siteConfig", key = "'public'")
    public void deleteConfig(Long id) {
        siteConfigMapper.deleteById(id);
    }
}
