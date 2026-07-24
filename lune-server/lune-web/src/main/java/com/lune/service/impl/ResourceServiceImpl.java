package com.lune.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Resource;
import com.lune.mapper.ResourceMapper;
import com.lune.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final Path uploadDir;

    public ResourceServiceImpl(ResourceMapper resourceMapper, @Value("${app.upload.path}") String uploadPath) {
        this.resourceMapper = resourceMapper;
        Path p = Paths.get(uploadPath);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(uploadPath);
        }
        this.uploadDir = p.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + this.uploadDir, e);
        }
    }

    @Override
    public Resource upload(MultipartFile file) {
        try {
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + "." + ext;
            File target = uploadDir.resolve(filename).toFile();
            file.transferTo(target);
            Resource resource = new Resource();
            resource.setFilename(file.getOriginalFilename());
            resource.setPath("/upload/" + filename);
            resource.setSize(file.getSize());
            resource.setMimeType(file.getContentType());
            resource.setType(file.getContentType() != null && file.getContentType().startsWith("image/") ? "image" : "file");
            resource.setStoreType("local");
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
    public PageResult<Resource> listResources(int page, int size) {
        var result = resourceMapper.selectPage(new Page<>(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Resource>()
                        .orderByDesc(Resource::getCreateTime));
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void deleteResource(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource != null) {
            try {
                Path filePath = uploadDir.resolve(Paths.get(resource.getPath()).getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
            resourceMapper.deleteById(id);
        }
    }
}
