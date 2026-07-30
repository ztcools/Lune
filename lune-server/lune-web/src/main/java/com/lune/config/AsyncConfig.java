package com.lune.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 访问日志专用线程池 + 开启定时任务（访问日志保留清理）。
 *
 * <p>不用 Spring Boot 默认的 {@code applicationTaskExecutor}：它的队列是无界的，
 * 突发流量下会把待写日志无限堆在堆里 —— 而生产 JVM 只有 {@code -Xmx512m}。
 * 这里改成有界队列 + {@link ThreadPoolExecutor.DiscardPolicy}：
 * 队列满时直接丢弃统计任务。统计数据宁可少几条，也不能因为它拖慢或拖垮正常请求。
 */
@Configuration
@EnableScheduling
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean("visitLogExecutor")
    public Executor visitLogExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("visit-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        // 关闭时不等待：统计任务不值得延长停机时间
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        log.info("[Async] visitLogExecutor 就绪（core=2 max=4 queue=500 满则丢弃）");
        return executor;
    }
}
