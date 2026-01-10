package com.web.opentelemetry.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

@Configuration
@EnableAsync
public class AsyncThreadConfig {
    private static final Logger logger = LoggerFactory.getLogger(AsyncThreadConfig.class);

    @Value("${spring.threads.virtual.enabled:false}")
    private boolean virtualThreadsEnabled;

    @Value("${spring.task.execution.thread-name-prefix:async-thread-}")
    private String threadNamePrefix;

    @Value("${spring.task.execution.pool.core-size:10}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size:100}")
    private int maxPoolSize;

    @Bean
    public TaskDecorator taskDecorator() {
        // Custom decorator that propagates MDC context to async threads (including virtual threads)
        return runnable -> {
            // Capture MDC context from the calling thread
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                try {
                    // Set the captured MDC context in the async thread
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // Execute the actual task
                    runnable.run();
                } finally {
                    // Clean up MDC after task execution
                    MDC.clear();
                }
            };
        };
    }

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor taskExecutor(TaskDecorator taskDecorator) {
        if (virtualThreadsEnabled) {
            logger.info("Using Virtual Thread Executor for async tasks with name prefix: {}", threadNamePrefix);
            SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
            asyncTaskExecutor.setTaskDecorator(taskDecorator);
            asyncTaskExecutor.setVirtualThreads(true);
            asyncTaskExecutor.setThreadNamePrefix("virtual-" + threadNamePrefix);
            //asyncTaskExecutor.setThreadFactory(Thread.ofVirtual().name("virtual-" + threadNamePrefix, 0).factory());
            asyncTaskExecutor.setTaskTerminationTimeout(5000); // ensure wait for task termination
            return asyncTaskExecutor;
        } else {
            logger.info("Using Custom ThreadPoolTaskExecutor with thread-name-prefix: {}", threadNamePrefix);
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setTaskDecorator(taskDecorator);
            executor.setCorePoolSize(corePoolSize);
            executor.setMaxPoolSize(maxPoolSize);
            executor.setQueueCapacity(50);
            executor.setThreadNamePrefix("regular-" + threadNamePrefix);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            logger.info("ThreadPoolTaskExecutor initialized successfully with {} core threads and {} max threads",
                    corePoolSize, maxPoolSize);
            return executor;
        }
    }
}
