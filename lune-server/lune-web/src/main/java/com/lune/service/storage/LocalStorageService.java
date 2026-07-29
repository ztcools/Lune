package com.lune.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地磁盘存储（默认实现）。文件写入 {@code app.upload.path}，
 * 由 Nginx 的 /upload/ location 直接提供访问。
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path uploadDir;

    public LocalStorageService(@Value("${app.upload.path}") String uploadPath) {
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
    public String store(MultipartFile file, String filename) throws IOException {
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());
        return "/upload/" + filename;
    }

    @Override
    public void delete(String path) throws IOException {
        String filename = Paths.get(path).getFileName().toString();
        Files.deleteIfExists(uploadDir.resolve(filename));
    }

    @Override
    public String storeType() {
        return "local";
    }
}
