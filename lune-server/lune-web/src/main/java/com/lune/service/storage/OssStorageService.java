package com.lune.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 对象存储（OSS）实现 —— 预留扩展点，默认不启用。
 *
 * <p><b>启用方式</b>：配置 {@code app.storage.type=oss} 并填充下列
 * {@code app.oss.*} 配置后，把对应云厂商 SDK 依赖加入 pom 并实现
 * {@link #store} 方法即可。当前为占位实现，仅抛出友好异常，
 * 避免在未接入 SDK 时被误用。</p>
 *
 * <p><b>推荐接入（阿里云 OSS 示例）</b>：</p>
 * <pre>
 *   &lt;dependency&gt;
 *     &lt;groupId&gt;com.aliyun.oss&lt;/groupId&gt;
 *     &lt;artifactId&gt;aliyun-sdk-oss&lt;/artifactId&gt;
 *   &lt;/dependency&gt;
 *
 *   OSS client = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
 *   client.putObject(bucket, "upload/" + filename, file.getInputStream());
 *   return cdnDomain + "/upload/" + filename;
 * </pre>
 *
 * <p>腾讯云 COS、七牛、MinIO 同理，只需替换 SDK 调用。</p>
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
    /** 可选 CDN 加速域名，配置后返回该域名下的 URL */
    @Value("${app.oss.cdn-domain:}")
    private String cdnDomain;

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        // TODO: 接入云厂商 SDK 后实现真实上传。
        // 当前为占位实现：提示需要完成 OSS 接入。
        log.error("OSS 存储已启用但尚未接入云厂商 SDK: endpoint={} bucket={}", endpoint, bucket);
        throw new IOException("OSS 存储尚未接入 SDK，请实现 OssStorageService.store() 或改用 app.storage.type=local");
    }

    @Override
    public String storeType() {
        return "oss";
    }
}
