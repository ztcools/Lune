package com.lune.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${app.upload.path}")
    private String rawUploadPath;

    private String resolvedUploadPath;

    /**
     * 启动时校验并规范化上传目录路径，防止路径遍历攻击。
     * 若路径不存在则自动创建；若包含 ../ 等非法跳转则拒绝启动。
     */
    @PostConstruct
    public void init() {
        Path p = Paths.get(rawUploadPath);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(rawUploadPath);
        }
        Path normalized = p.toAbsolutePath().normalize();
        try {
            Files.createDirectories(normalized);
        } catch (Exception e) {
            throw new IllegalStateException("无法创建上传目录: " + normalized, e);
        }
        // 校验 canonical 路径与 normalized 一致（防止符号链接绕过）
        try {
            Path real = normalized.toRealPath();
            if (!real.equals(normalized)) {
                log.warn("上传目录经过符号链接解析: {} → {}", normalized, real);
            }
            this.resolvedUploadPath = real.toString();
        } catch (Exception e) {
            // 解析失败（如目录还不存在，但已在上方 createDirectories 创建）
            this.resolvedUploadPath = normalized.toString();
        }
        // 安全检查：路径不得包含 ../
        if (resolvedUploadPath.contains("..")) {
            throw new IllegalStateException("上传目录包含非法路径: " + resolvedUploadPath);
        }
        log.info("上传目录: {}", resolvedUploadPath);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + resolvedUploadPath + "/");
    }
}
