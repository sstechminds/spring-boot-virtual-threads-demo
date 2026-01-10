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
    public CompletableFuture<String> fetchDogImageAsync(String callId) {
        logger.info("Executing async-spring method: {} on thread: {}", callId, Thread.currentThread().getName());

        // Retrieve the value from MDC within the async thread
        String traceId = MDC.get("traceId");
        logger.info("Async task executing with traceId: {}", traceId);

        try {
            String response = restClient.get()
                    .uri("https://dog.ceo/api/breeds/image/random")
                    .retrieve()
                    .body(String.class);

            logger.info("Completed async-spring method: {}", callId);
            return CompletableFuture.completedFuture(traceId);
        } catch (Exception e) {
            logger.error("Failed async-spring method: {}", callId, e);
            return CompletableFuture.completedFuture("Failed to fetch data: " + e.getMessage());
        }
    }
}
