package com.lune.service;

import com.lune.entity.VisitLog;
import com.lune.mapper.VisitLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 访问日志异步写入。
 *
 * <p>单独抽成一个 Bean 而不是留在 {@code VisitLogInterceptor} 里，是因为 {@code @Async}
 * 依赖 Spring AOP 代理：同类内部直接调用自己的 {@code @Async} 方法会绕过代理，
 * 注解静默失效、方法退化成同步执行 —— 也就是说 GeoIP 查询和 INSERT 全都压在请求线程上。
 * 跨 Bean 调用才会真正走异步。
 */
@Service
public class VisitLogRecorder {

    private static final Logger log = LoggerFactory.getLogger(VisitLogRecorder.class);

    private final VisitLogMapper visitLogMapper;
    private final GeoIpService geoIpService;

    public VisitLogRecorder(VisitLogMapper visitLogMapper, GeoIpService geoIpService) {
        this.visitLogMapper = visitLogMapper;
        this.geoIpService = geoIpService;
    }

    @Async("visitLogExecutor")
    public void record(String ip, String userAgent, String path, String method) {
        try {
            var entry = new VisitLog();
            entry.setIp(ip);
            entry.setUserAgent(truncate(userAgent, 500));
            entry.setPath(truncate(path, 200));
            entry.setMethod(method);

            geoIpService.lookup(ip).ifPresent(loc -> {
                entry.setCountry(truncate(loc.country(), 50));
                entry.setProvince(truncate(loc.province(), 50));
                entry.setCity(truncate(loc.city(), 50));
                entry.setLongitude(BigDecimal.valueOf(loc.longitude()));
                entry.setLatitude(BigDecimal.valueOf(loc.latitude()));
            });

            visitLogMapper.insert(entry);
        } catch (Exception e) {
            // 统计失败绝不能影响正常访问，这里只记日志
            log.warn("[VisitLog] 记录失败 ip={} path={}：{}", ip, path, e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
