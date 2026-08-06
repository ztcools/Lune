package com.lune.service.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 腾讯云 COS 对象存储实现。
 *
 * <p><b>启用方式</b>：配置 {@code app.storage.type=oss} 并填充
 * {@code app.oss.*} 环境变量后自动激活，替代默认的 LocalStorageService。</p>
 *
 * <p>文件上传到 COS 后返回 CDN 加速域名下的 URL（{@code https://res.ztcools.com/upload/xxx}），
 * 浏览器直接通过 CDN 加载，不经过服务器 nginx。</p>
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "oss")
public class OssStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(OssStorageService.class);

    @Value("${app.oss.endpoint:}")
    private String endpoint;
    @Value("${app.oss.bucket:}")
    private String bucket;
    @Value("${app.oss.access-key:}")
    private String accessKey;
    @Value("${app.oss.secret-key:}")
    private String secretKey;
    /** CDN 加速域名（如 https://res.ztcools.com），配置后返回该域名下的 URL */
    @Value("${app.oss.cdn-domain:}")
    private String cdnDomain;

    private COSClient client;
    /** COS 地域，从 endpoint 提取（如 cos.ap-guangzhou.myqcloud.com → ap-guangzhou） */
    private String region;

    @PostConstruct
    public void init() {
        // endpoint 格式：cos.ap-guangzhou.myqcloud.com → region = ap-guangzhou
        region = endpoint.replaceFirst("^cos\\.", "").replaceFirst("\\.myqcloud\\.com$", "");
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        this.client = new COSClient(cred, config);
        log.info("OSS storage enabled: bucket={} region={} cdn={}", bucket, region, cdnDomain);
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        String key = "upload/" + filename;
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            String mime = file.getContentType();
            if (mime != null) meta.setContentType(mime);

            PutObjectRequest req = new PutObjectRequest(bucket, key,
                    file.getInputStream(), meta);
            client.putObject(req);

            String url = (cdnDomain != null && !cdnDomain.isBlank())
                    ? cdnDomain.replaceAll("/+$", "") + "/" + key
                    : "https://" + bucket + ".cos." + region + ".myqcloud.com/" + key;
            log.debug("Uploaded to COS: {} → {}", filename, url);
            return url;
        } catch (CosClientException e) {
            log.error("COS upload failed: {}", filename, e);
            throw new IOException("文件上传到对象存储失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) throws IOException {
        if (path == null || path.isBlank()) return;
        // 从完整 URL 提取 key（upload/uuid.ext）
        String key = extractKey(path);
        if (key == null) {
            log.warn("Cannot parse COS key from path: {}", path);
            return;
        }
        try {
            client.deleteObject(bucket, key);
            log.debug("Deleted from COS: {}", key);
        } catch (CosClientException e) {
            log.warn("COS delete failed for key={}: {}", key, e.getMessage());
            // 删除失败不中断请求 —— 文件本身会随 bucket 生命周期自动清理
        }
    }

    @Override
    public String storeType() {
        return "oss";
    }

    /**
     * 从 CDN URL 或 COS 原始 URL 中提取 object key。
     * 支持：
     *   https://res.ztcools.com/upload/xxx.jpg     → upload/xxx.jpg
     *   https://bucket.cos.ap-guangzhou.myqcloud.com/upload/xxx.jpg → upload/xxx.jpg
     */
    static String extractKey(String url) {
        if (url == null) return null;
        int i = url.indexOf("/upload/");
        if (i >= 0) return url.substring(i + 1);
        return null;
    }
}
