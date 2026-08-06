package com.lune.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Resource;
import com.lune.mapper.ResourceMapper;
import com.lune.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final ResourceMapper resourceMapper;
    private final com.lune.service.storage.StorageService storageService;

    public ResourceServiceImpl(ResourceMapper resourceMapper,
                               com.lune.service.storage.StorageService storageService) {
        this.resourceMapper = resourceMapper;
        this.storageService = storageService;
    }

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of("jpg","jpeg","png","gif","svg","webp","bmp","ico","mp4","webm","mp3","wav","pdf","zip");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @Override
    public Resource upload(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("文件不能为空");
        if (file.getSize() > MAX_FILE_SIZE) throw new BusinessException("文件大小不能超过 50MB");
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: ." + ext);
        }
        // SVG 可携带脚本，存在 XSS 风险，直接拒绝
        if ("svg".equalsIgnoreCase(ext)) {
            throw new BusinessException("出于安全考虑，暂不支持 SVG 上传");
        }
        try {
            String filename = UUID.randomUUID().toString() + "." + ext;
            String path = storageService.store(file, filename);
            Resource resource = new Resource();
            resource.setFilename(file.getOriginalFilename());
            resource.setPath(path);
            resource.setSize(file.getSize());
            resource.setMimeType(file.getContentType());
            resource.setType(file.getContentType() != null && file.getContentType().startsWith("image/") ? "image" : "file");
            resource.setStoreType(storageService.storeType());
            resourceMapper.insert(resource);
            return resource;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "bin";
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1) : "bin";
    }

    @Override
    public Resource importFromUrl(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        int qi = filename.indexOf('?');
        if (qi > 0) filename = filename.substring(0, qi);
        if (filename.isEmpty()) filename = "imported_" + System.currentTimeMillis();

        String ext = getExtension(filename);
        if (ext.equals("bin") || ext.length() > 5) {
            String lower = url.toLowerCase();
            if (lower.contains(".jpg") || lower.contains(".jpeg")) ext = "jpg";
            else if (lower.contains(".png")) ext = "png";
            else if (lower.contains(".gif")) ext = "gif";
            else if (lower.contains(".svg")) ext = "svg";
            else if (lower.contains(".webp")) ext = "webp";
            else ext = "jpg";
        }

        String storedPath = url;
        Resource resource = new Resource();
        resource.setFilename(filename);
        resource.setPath(storedPath);
        resource.setSize(0L);
        resource.setMimeType("image/" + ext);
        resource.setType("image");
        resource.setStoreType("remote");
        resourceMapper.insert(resource);
        return resource;
    }

    @Override
    public PageResult<Resource> listResources(int page, int size, String type) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Resource>()
                .orderByDesc(Resource::getCreateTime);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Resource::getType, type);
        }
        var result = resourceMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void deleteResource(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource != null) {
            // 本地存储：从磁盘删除；OSS：通过 COS SDK 删除
            if ("local".equals(resource.getStoreType()) || "oss".equals(resource.getStoreType())) {
                try {
                    storageService.delete(resource.getPath());
                } catch (IOException e) {
                    log.warn("删除文件失败: {}", resource.getPath(), e);
                }
            }
            resourceMapper.deleteById(id);
        }
    }
}
