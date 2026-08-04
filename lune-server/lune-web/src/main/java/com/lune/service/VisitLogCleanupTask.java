package com.lune.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.entity.VisitLog;
import com.lune.mapper.VisitLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 访问日志保留期清理。
 *
 * <p>visit_log 每天每个独立 IP 写一行（前端每日 ping 一次，Redis 去重）。
 * 默认保留 90 天，足够支撑「最近 90 天趋势」这个最大查询窗口。
 *
 * <p>分批删除而不是一条 DELETE 删完：一次性删掉几十万行会长时间持有行锁、
 * 并让 binlog 瞬间膨胀，在 2C4G 的机器上足以拖慢整站。
 *
 * <p>清理完成后执行 OPTIMIZE TABLE 回收磁盘空间。
 */
@Component
public class VisitLogCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(VisitLogCleanupTask.class);
    private static final int BATCH_SIZE = 2000;
    private static final int MAX_BATCHES = 50; // 单次最多删 10 万行，剩下的留给下一次

    private final VisitLogMapper visitLogMapper;

    /** 保留天数，0 或负数表示不清理 */
    @Value("${app.visit-log.retention-days:90}")
    private int retentionDays;

    public VisitLogCleanupTask(VisitLogMapper visitLogMapper) {
        this.visitLogMapper = visitLogMapper;
    }

    /** 每天凌晨 4:20 执行，避开 3:00 的数据库备份 */
    @Scheduled(cron = "0 20 4 * * ?")
    public void cleanup() {
        if (retentionDays <= 0) return;
        LocalDateTime cutoff = LocalDateTime.of(LocalDate.now().minusDays(retentionDays), LocalTime.MIN);

        long deletedTotal = 0;
        for (int i = 0; i < MAX_BATCHES; i++) {
            int deleted = visitLogMapper.delete(
                new LambdaQueryWrapper<VisitLog>()
                    .lt(VisitLog::getCreateTime, cutoff)
                    .last("LIMIT " + BATCH_SIZE)); // 常量，无注入风险
            deletedTotal += deleted;
            if (deleted < BATCH_SIZE) break;
        }
        if (deletedTotal > 0) {
            log.info("[VisitLog] 清理 {} 之前的访问日志，共 {} 行", cutoff.toLocalDate(), deletedTotal);
            // 大批量删除后回收表空间
            try {
                visitLogMapper.optimize();
                log.info("[VisitLog] OPTIMIZE TABLE 完成");
            } catch (Exception e) {
                log.warn("[VisitLog] OPTIMIZE TABLE 失败：{}", e.getMessage());
            }
        }
    }
}
