package com.lune.agent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Agent 服务线程池配置。
 *
 * <p>有界线程池替代 {@code new Thread()}：控制并发数，防止资源耗尽。
 * 队列满时由调用线程执行（CallerRunsPolicy），形成自然背压。</p>
 */
@Configuration
@EnableAsync
public class AgentExecutorConfig {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutorConfig.class);

    @Bean("agentTaskExecutor")
    public ThreadPoolTaskExecutor agentTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("agent-pipe-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn("Agent thread pool exhausted: active={}, queue={}, pool={}",
                        e.getActiveCount(), e.getQueue().size(), e.getPoolSize());
                super.rejectedExecution(r, e);
            }
        });
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("Agent executor configured: core=4, max=16, queue=100, policy=CallerRuns");
        return executor;
    }
}
