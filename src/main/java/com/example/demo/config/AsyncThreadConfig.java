package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncThreadConfig implements AsyncConfigurer {
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
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator(); // This depends on micrometer!
    }

    @Bean
    public AsyncTaskExecutor taskExecutor(ContextPropagatingTaskDecorator decorator) {
        if (virtualThreadsEnabled) {
            logger.info("Using Virtual Thread Executor for async tasks with name prefix: {}", threadNamePrefix);
            SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
            asyncTaskExecutor.setVirtualThreads(true); // virtual threads enabled
            asyncTaskExecutor.setTaskDecorator(contextPropagatingTaskDecorator());
            asyncTaskExecutor.setThreadFactory(Thread.ofVirtual().name(threadNamePrefix, 0).factory());
            asyncTaskExecutor.setTaskTerminationTimeout(5000); // ensure wait for task termination
            return asyncTaskExecutor;
        } else {
            logger.info("Using Custom ThreadPoolTaskExecutor with thread-name-prefix: {}", threadNamePrefix);
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(corePoolSize);
            executor.setMaxPoolSize(maxPoolSize);
            executor.setQueueCapacity(50);
            executor.setThreadNamePrefix(threadNamePrefix);
            executor.setTaskDecorator(decorator);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setAwaitTerminationSeconds(60);
            executor.initialize();
            logger.info("ThreadPoolTaskExecutor initialized successfully with {} core threads and {} max threads",
                    corePoolSize, maxPoolSize);
            return executor;
        }
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor(contextPropagatingTaskDecorator());
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
            logger.error("Uncaught async exception in method: {} with params: {}", method.getName(), params, ex);
    }
}
