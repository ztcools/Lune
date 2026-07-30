package com.lune.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lune.common.ChinaRegion;
import com.lune.common.Result;
import com.lune.entity.VisitLog;
import com.lune.mapper.VisitLogMapper;
import com.lune.service.GeoIpService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 后台访问统计接口：访问趋势、地理分布、IP 排行。
 *
 * <p><b>聚合一律下推到 SQL。</b>早先的实现是把 visit_log 整表 {@code selectList} 到内存里再用
 * Stream 分组 —— 生产 JVM 只有 {@code -Xmx512m}，日志表涨到几十万行就会直接 OOM，
 * 而且是「平时好用、用久了突然挂」的那种故障。现在全部走 COUNT / GROUP BY / LIMIT，
 * 返回行数只与维度基数（天数 ≤90、省份 ≤34、Top N ≤100）相关，与表规模无关。
 */
@RestController
@RequestMapping("/api/admin/visit-stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVisitStatsController {

    private final VisitLogMapper visitLogMapper;
    private final GeoIpService geoIpService;

    public AdminVisitStatsController(VisitLogMapper visitLogMapper, GeoIpService geoIpService) {
        this.visitLogMapper = visitLogMapper;
        this.geoIpService = geoIpService;
    }

    /** 总览统计 GET /api/admin/visit-stats/summary */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        Long total = visitLogMapper.selectCount(null);
        Long today = visitLogMapper.selectCount(
            new LambdaQueryWrapper<VisitLog>().ge(VisitLog::getCreateTime, todayStart));
        Long yesterday = visitLogMapper.selectCount(
            new LambdaQueryWrapper<VisitLog>()
                .ge(VisitLog::getCreateTime, yesterdayStart)
                .lt(VisitLog::getCreateTime, todayStart));

        // COUNT(DISTINCT ip) 交给数据库，不再把所有 IP 拉进堆里去 distinct
        long uniqueIps = countDistinctIp(null);
        long todayUniqueIps = countDistinctIp(todayStart);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("today", today);
        result.put("yesterday", yesterday);
        result.put("uniqueIps", uniqueIps);
        result.put("todayUniqueIps", todayUniqueIps);
        // 地理库缺失时前端需要提示，否则地图空白会被误认为「没人访问」
        result.put("geoAvailable", geoIpService.isAvailable());
        return Result.success(result);
    }

    private long countDistinctIp(LocalDateTime since) {
        QueryWrapper<VisitLog> qw = new QueryWrapper<>();
        qw.select("COUNT(DISTINCT ip) AS c");
        if (since != null) qw.ge("create_time", since);
        List<Map<String, Object>> rows = visitLogMapper.selectMaps(qw);
        return rows.isEmpty() ? 0L : toLong(rows.get(0).get("c"));
    }

    /** 访问趋势（最近 N 天，按日聚合）GET /api/admin/visit-stats/trend?days=30 */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "30") int days) {
        days = Math.min(Math.max(days, 7), 90);
        LocalDate from = LocalDate.now().minusDays(days - 1L);
        LocalDateTime start = LocalDateTime.of(from, LocalTime.MIN);

        // 按日分组在 SQL 里完成，最多返回 days 行
        QueryWrapper<VisitLog> qw = new QueryWrapper<>();
        qw.select("DATE(create_time) AS d", "COUNT(*) AS c", "COUNT(DISTINCT ip) AS u")
          .ge("create_time", start)
          .groupBy("d");
        List<Map<String, Object>> rows = visitLogMapper.selectMaps(qw);

        Map<String, Map<String, Object>> byDate = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object d = row.get("d");
            if (d != null) byDate.put(d.toString(), row);
        }

        // 补齐没有访问的日期，保证折线图 X 轴连续
        List<Map<String, Object>> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate date = from.plusDays(i);
            Map<String, Object> row = byDate.get(date.toString());
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", row != null ? toLong(row.get("c")) : 0L);
            item.put("uniqueIps", row != null ? toLong(row.get("u")) : 0L);
            result.add(item);
        }
        return Result.success(result);
    }

    /** 中国地图数据（按省份聚合）GET /api/admin/visit-stats/map */
    @GetMapping("/map")
    public Result<List<Map<String, Object>>> map() {
        QueryWrapper<VisitLog> qw = new QueryWrapper<>();
        qw.select("province AS name", "COUNT(*) AS value")
          .isNotNull("province")
          .ne("province", "")
          .ne("province", ChinaRegion.UNKNOWN)
          .ne("province", "内网")
          .groupBy("province")
          .orderByDesc("value");
        List<Map<String, Object>> rows = visitLogMapper.selectMaps(qw);

        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row.get("name"));
            item.put("value", toLong(row.get("value")));
            result.add(item);
        }
        return Result.success(result);
    }

    /** Top IP 排行 GET /api/admin/visit-stats/top-ips?limit=20 */
    @GetMapping("/top-ips")
    public Result<List<Map<String, Object>>> topIps(@RequestParam(defaultValue = "20") int limit) {
        limit = Math.min(Math.max(limit, 5), 100);

        // 地区是由 IP 推导出来的，同一 IP 各行取值一致，故用 MAX() 取代「按时间取最新一条」，
        // 这样一条 GROUP BY 就够了，不必把全表拉出来排序。
        QueryWrapper<VisitLog> qw = new QueryWrapper<>();
        qw.select("ip AS ip", "COUNT(*) AS c", "MAX(create_time) AS last_visit",
                  "MAX(country) AS country", "MAX(province) AS province", "MAX(city) AS city")
          .groupBy("ip")
          .orderByDesc("c")
          .last("LIMIT " + limit); // limit 已 clamp 为 int，无注入风险
        List<Map<String, Object>> rows = visitLogMapper.selectMaps(qw);

        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String country = str(row.get("country"));
            String province = str(row.get("province"));
            String city = str(row.get("city"));
            Map<String, Object> item = new HashMap<>();
            item.put("ip", row.get("ip"));
            item.put("count", toLong(row.get("c")));
            item.put("country", country);
            item.put("province", province);
            item.put("city", city);
            item.put("location", buildLocation(country, province, city));
            item.put("lastVisit", row.get("last_visit"));
            result.add(item);
        }
        return Result.success(result);
    }

    /** 最近访问记录 GET /api/admin/visit-stats/recent?limit=50 */
    @GetMapping("/recent")
    public Result<List<Map<String, Object>>> recent(@RequestParam(defaultValue = "50") int limit) {
        limit = Math.min(Math.max(limit, 10), 200);

        List<VisitLog> logs = visitLogMapper.selectList(
            new LambdaQueryWrapper<VisitLog>()
                .orderByDesc(VisitLog::getCreateTime)
                .last("LIMIT " + limit)); // limit 已 clamp 为 int，无注入风险

        List<Map<String, Object>> result = new ArrayList<>(logs.size());
        for (VisitLog l : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", l.getId());
            item.put("ip", l.getIp());
            item.put("location", buildLocation(l.getCountry(), l.getProvince(), l.getCity()));
            item.put("province", l.getProvince());
            item.put("city", l.getCity());
            item.put("path", l.getPath());
            item.put("method", l.getMethod());
            item.put("userAgent", l.getUserAgent());
            item.put("createTime", l.getCreateTime());
            result.add(item);
        }
        return Result.success(result);
    }

    private String buildLocation(String country, String province, String city) {
        if (country == null || country.isEmpty()) return ChinaRegion.UNKNOWN;
        if ("内网".equals(country)) return "内网";
        StringBuilder sb = new StringBuilder(country);
        if (isMeaningful(province)) sb.append(' ').append(province);
        if (isMeaningful(city) && !city.equals(province)) sb.append(' ').append(city);
        return sb.toString();
    }

    private boolean isMeaningful(String v) {
        return v != null && !v.isEmpty() && !ChinaRegion.UNKNOWN.equals(v);
    }

    private static String str(Object o) {
        return o == null ? ChinaRegion.UNKNOWN : o.toString();
    }

    private static long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (NumberFormatException e) { return 0L; }
    }
}
