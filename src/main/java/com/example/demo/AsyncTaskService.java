package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncTaskService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);

    @Autowired
    RestClient restClient;

    @Async("taskExecutor")
    public CompletableFuture<String> fetchDataAsync(String callId) {
        logger.info("Executing async-spring method: {} on thread: {}", callId, Thread.currentThread().getName());

        // Retrieve the value from MDC within the async thread
        String requestId = MDC.get("requestId");
        logger.info("Async task executing with requestId: {}", requestId);
        MDC.put("requestId", requestId);
        try {
            String response = restClient.get()
                    .uri("http://localhost:8080/api/info")
                    .retrieve()
                    .body(String.class);

            logger.info("Completed async-spring method: {}", callId); //TODO: TraceId missing here???
            return CompletableFuture.completedFuture(requestId);
        } catch (Exception e) {
            logger.error("Failed async-spring method: {}", callId, e);
            return CompletableFuture.completedFuture("Failed to fetch data: " + e.getMessage());
        }
    }
}
