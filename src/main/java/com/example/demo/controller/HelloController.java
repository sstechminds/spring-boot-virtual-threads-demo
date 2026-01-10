package com.example.demo.controller;

import com.example.demo.AsyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    RestClient restClient;

    @Autowired
    AsyncTaskService asyncTaskService;

    @Autowired
    AsyncTaskExecutor taskExecutor;

    @GetMapping("/info/restclient")
    public String info() {
        String response;
        try {
            response = restClient.get()
                    .uri("https://dog.ceo/api/breeds/image/random")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            response = "Failed to fetch data from Google";
        }
        return response;
    }

    @GetMapping("/info/async")
    public String async() {
        logger.info("Starting manual async calls with custom executor");

        //MDC.put("traceId", "traceAsync");
        MDC.put("requestId", "async-spring-requestId-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));

        // Create two async tasks to make API calls concurrently using our custom executor
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Executing async call 1 on thread: {}", Thread.currentThread().getName());
                var resp = restClient.get()
                        .uri("https://dog.ceo/api/breeds/image/random")
                        .retrieve()
                        .body(String.class);
                return MDC.get("traceId") + " " + resp;
            } catch (Exception e) {
                return "Failed to fetch data from first API call: " + e.getMessage();
            }
        }, taskExecutor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Executing async call 2 on thread: {}", Thread.currentThread().getName());
                var resp = restClient.get()
                        .uri("https://dog.ceo/api/breeds/image/random")
                        .retrieve()
                        .body(String.class);
                return MDC.get("traceId") + " " + resp;
            } catch (Exception e) {
                return "Failed to fetch data from second API call: " + e.getMessage();
            }
        }, taskExecutor);

        // Wait for both futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2);

        try {
            allFutures.get(); // Wait for both to complete
            String result1 = future1.get();
            String result2 = future2.get();
            logger.info("All async calls completed");
            var resp = "{\n  \"call1\": " + result1 + ",\n  \"call2\": " + result2 + "\n}";
            logger.info(resp);
            return resp;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error waiting for async calls", e);
            return "Error waiting for async calls: " + e.getMessage();
        }
    }

    @GetMapping("/info/async-spring")
    public String asyncSpring() {
        logger.info("Starting async Spring method calls");
        MDC.put("requestId", "async-spring-requestId-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        logger.info("Async process started...");

        // Call async methods
        CompletableFuture<String> future1 = asyncTaskService.fetchDogImageAsync("call1");
        CompletableFuture<String> future2 = asyncTaskService.fetchDogImageAsync("call2");

        // Wait for both to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2);

        try {
            allFutures.get();
            String result1 = future1.get();
            String result2 = future2.get();

            logger.info("All async calls completed");
            return "{\n  \"call1\": " + result1 + ",\n  \"call2\": " + result2 + "\n}";
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error waiting for async calls", e);
            return "Error waiting for async calls: " + e.getMessage();
        }
    }
}

