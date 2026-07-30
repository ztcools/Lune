package com.lune.service;

import com.lune.common.ChinaRegion;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * GeoLite2 IP 地理位置查询服务
 * 使用 MaxMind GeoLite2-City 离线数据库，支持国家/省份/城市/经纬度查询，毫秒级响应。
 *
 * <p>加载顺序：优先读 {@code app.geoip.path} 指向的文件（生产以只读挂载提供，
 * 避免 66MB 的库被打进 jar 与镜像、每次部署都随 docker save 重传一遍）；
 * 找不到再回退到 classpath（本地开发方便）。
 *
 * <p>省份名称统一经 {@link ChinaRegion} 归一化后返回，保证与前端地图的 name 字段一致。
 */
@Service
public class GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpService.class);
    private static final String CLASSPATH_DB = "geoip/GeoLite2-City.mmdb";

    /** 外部数据库路径，留空则仅尝试 classpath */
    @Value("${app.geoip.path:}")
    private String externalPath;

    private DatabaseReader reader;
    private File tempCopy;

    @PostConstruct
    public void init() {
        // 1) 外部文件优先：直接以 File 打开，无需复制，启动快且不占额外磁盘
        if (externalPath != null && !externalPath.isBlank()) {
            File f = new File(externalPath);
            if (f.isFile() && f.canRead()) {
                try {
                    this.reader = new DatabaseReader.Builder(f).build();
                    log.info("[GeoIp] 已从外部路径加载 GeoLite2 数据库：{}", externalPath);
                    return;
                } catch (IOException e) {
                    log.warn("[GeoIp] 外部数据库加载失败（{}），尝试 classpath：{}", externalPath, e.getMessage());
                }
            } else {
                log.warn("[GeoIp] 外部数据库不可读：{}，尝试 classpath", externalPath);
            }
        }
        // 2) classpath 回退：需要复制成临时文件，DatabaseReader 只接受 File/RandomAccessFile
        try {
            ClassPathResource resource = new ClassPathResource(CLASSPATH_DB);
            if (!resource.exists()) {
                log.warn("[GeoIp] 未找到 GeoLite2 数据库（外部路径与 classpath 均无），"
                    + "IP 地理查询不可用，访问统计将只记录 IP 而无地区");
                return;
            }
            try (InputStream in = resource.getInputStream()) {
                tempCopy = File.createTempFile("geolite2", ".mmdb");
                tempCopy.deleteOnExit();
                Files.copy(in, tempCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
                this.reader = new DatabaseReader.Builder(tempCopy).build();
                log.info("[GeoIp] 已从 classpath 加载 GeoLite2 数据库");
            }
        } catch (IOException e) {
            log.error("[GeoIp] 数据库加载失败：{}", e.getMessage());
        }
    }

    /** 数据库是否可用，供统计接口提示前端 */
    public boolean isAvailable() {
        return reader != null;
    }

    @PreDestroy
    public void destroy() {
        if (reader != null) {
            try { reader.close(); } catch (IOException ignored) {}
        }
        if (tempCopy != null) {
            try { Files.deleteIfExists(tempCopy.toPath()); } catch (IOException ignored) {}
        }
    }

    /**
     * 查询 IP 地理位置
     * @param ip IPv4/IPv6 地址
     * @return Optional<IpLocation>，查询失败或库未加载返回 empty
     */
    public Optional<IpLocation> lookup(String ip) {
        if (reader == null || ip == null || ip.isEmpty()) return Optional.empty();
        // 内网/回环地址直接返回
        if (isPrivate(ip)) {
            return Optional.of(new IpLocation("内网", "内网", "内网", 0.0, 0.0));
        }
        try {
            CityResponse resp = reader.city(InetAddress.getByName(ip));
            String country = Optional.ofNullable(resp.getCountry().getNames().get("zh-CN"))
                .orElse(resp.getCountry().getName());
            String rawProvince = resp.getMostSpecificSubdivision() != null
                ? Optional.ofNullable(resp.getMostSpecificSubdivision().getNames().get("zh-CN"))
                    .orElse(resp.getMostSpecificSubdivision().getName())
                : null;
            // 归一化成 china.json 的省级全称，否则前端地图无法着色
            String province = ChinaRegion.normalizeProvince(resp.getCountry().getIsoCode(), rawProvince);
            String city = resp.getCity() != null
                ? Optional.ofNullable(resp.getCity().getNames().get("zh-CN"))
                    .orElse(resp.getCity().getName())
                : "";
            // getLocation() 理论上非空，但坐标可能缺失；此处全部兜底为 0
            var loc = resp.getLocation();
            Double lng = loc != null ? loc.getLongitude() : null;
            Double lat = loc != null ? loc.getLatitude() : null;
            return Optional.of(new IpLocation(
                country != null ? country : ChinaRegion.UNKNOWN,
                province,
                city != null && !city.isEmpty() ? city : ChinaRegion.UNKNOWN,
                lng != null ? lng : 0.0,
                lat != null ? lat : 0.0
            ));
        } catch (GeoIp2Exception | IOException | RuntimeException e) {
            // RuntimeException 一并兜住：地址格式异常/库内数据缺字段都不应影响请求
            return Optional.empty();
        }
    }

    /** 内网与回环地址判定（原实现漏了 172.17–172.31，Docker 默认网段正落在其中） */
    private boolean isPrivate(String ip) {
        if (ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1") || ip.startsWith("fe80:") || ip.startsWith("fc")) return true;
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("169.254.")) return true;
        // 172.16.0.0/12 → 172.16 ~ 172.31
        if (ip.startsWith("172.")) {
            int p1 = ip.indexOf('.'), p2 = ip.indexOf('.', p1 + 1);
            if (p2 > p1) {
                try {
                    int second = Integer.parseInt(ip.substring(p1 + 1, p2));
                    if (second >= 16 && second <= 31) return true;
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    /**
     * IP 地理位置记录
     */
    public record IpLocation(String country, String province, String city,
                              Double longitude, Double latitude) {}
}
