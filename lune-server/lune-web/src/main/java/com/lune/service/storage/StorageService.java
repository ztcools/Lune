package com.lune.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储抽象（本地 / 对象存储 OSS）。
 *
 * <p>当前默认实现为 {@link LocalStorageService}（本地磁盘，经 Nginx 提供）。
 * 上线接入 CDN/OSS 时，新增一个 {@code OssStorageService} 实现本接口，
 * 并在配置里把 {@code app.storage.type} 改为 {@code oss} 即可无缝切换，
 * 业务层（ResourceServiceImpl）无需改动。</p>
 */
public interface StorageService {

    /**
     * 存储一个上传文件，返回可访问的相对/绝对 URL 路径。
     *
     * @param file     上传文件
     * @param filename 归一化后的目标文件名（含扩展名）
     * @return 可访问路径（本地为 /upload/xxx，OSS 为完整 URL 或 CDN 路径）
     */
    String store(MultipartFile file, String filename) throws IOException;

    /**
     * 删除一个文件（按 store 返回的路径）。
     */
    default void delete(String path) throws IOException {
        // OSS 实现按需覆盖
    }

    /**
     * 存储类型标识，写入 resource.store_type。
     */
    String storeType();
}
