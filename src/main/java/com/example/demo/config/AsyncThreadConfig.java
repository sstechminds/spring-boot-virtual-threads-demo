package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncThreadConfig {
    private static final Logger logger = LoggerFactory.getLogger(AsyncThreadConfig.class);

    @Value("${spring.threads.virtual2.enabled:false}")
    private boolean virtualThreadsEnabled;

    @Value("${spring.task.execution.thread-name-prefix:async-thread-}")
    private String threadNamePrefix;

    @Value("${spring.task.execution.pool.core-size:10}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size:100}")
    private int maxPoolSize;

    @Bean
    public TaskDecorator taskDecorator() {
        // This standard Spring decorator uses the Micrometer Context Propagation library internally
        // to handle ThreadLocal-based contexts like SecurityContextHolder, MDC, etc.
        return new ContextPropagatingTaskDecorator(); // This depends on micrometer!
    }

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor taskExecutor(TaskDecorator taskDecorator) {
        if (virtualThreadsEnabled) {
            logger.info("Using Virtual Thread Executor for async tasks with name prefix: {}", threadNamePrefix);
            SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
            asyncTaskExecutor.setVirtualThreads(true);
            asyncTaskExecutor.setTaskDecorator(taskDecorator);
            asyncTaskExecutor.setThreadFactory(Thread.ofVirtual().name("virtual-" + threadNamePrefix, 0).factory());
            asyncTaskExecutor.setTaskTerminationTimeout(5000); // ensure wait for task termination
            return asyncTaskExecutor;
        } else {
            logger.info("Using Custom ThreadPoolTaskExecutor with thread-name-prefix: {}", threadNamePrefix);
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(corePoolSize);
            executor.setMaxPoolSize(maxPoolSize);
            executor.setQueueCapacity(50);
            executor.setThreadNamePrefix("regular-" + threadNamePrefix);
            executor.setTaskDecorator(taskDecorator);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            logger.info("ThreadPoolTaskExecutor initialized successfully with {} core threads and {} max threads",
                    corePoolSize, maxPoolSize);
            return executor;
        }
    }
}
