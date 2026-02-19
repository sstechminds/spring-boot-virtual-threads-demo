package com.web.opentelemetry.controller;

import com.web.opentelemetry.service.AsyncTaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

import static com.web.opentelemetry.interceptor.TraceHttpInterceptor.HEADER_REQUEST_ID;

@Slf4j
@RestController
@RequestMapping("/api")
public class ThreadsLogTraceController {
//    static final Logger log = LoggerFactory.getLogger(ThreadsLogTraceController.class);

    @Autowired
    RestClient restClient;

    @Autowired
    AsyncTaskService asyncTaskService;

    @Autowired
    AsyncTaskExecutor taskExecutor;

    @GetMapping("/info")
    public String info(HttpServletRequest request) {
        log.info("In the info endpoint");
        String response;
        try {
            response = restClient.get()
                    .uri("http://localhost:8080/api/internal")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            response = "Failed to fetch data from Google";
        }
        return response;
    }

    @GetMapping("/info/async")
    public String async(HttpServletRequest request) {
        log.info("Starting manual async calls from Main thread");
        // Create two async tasks to make API calls concurrently using our custom executor
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Executing async call 1 on thread: {}", Thread.currentThread().getName());
                var resp = restClient.get()
                        .uri("http://localhost:8080/api/info")
                        .retrieve()
                        .body(String.class);
                return resp;
            } catch (Exception e) {
                return "Failed to fetch data from first API call: " + e.getMessage();
            }
        }, taskExecutor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Executing async call 2 on thread: {}", Thread.currentThread().getName());
                var resp = restClient.get()
                        .uri("http://localhost:8080/api/info")
                        .retrieve()
                        .body(String.class);
                return resp;
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
            log.info("All async calls completed");
            var resp = "{\n  \"call1\": " + result1 + ",\n  \"call2\": " + result2 + "\n}";
            log.info(resp);
            return resp;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error waiting for async calls", e);
            return "Error waiting for async calls: " + e.getMessage();
        }
    }

    @GetMapping("/info/async-spring")
    public String asyncSpring(HttpServletRequest request) {
        log.info("Starting Spring async calls from Main thread");
        // Call async methods
        CompletableFuture<String> future1 = asyncTaskService.fetchDataAsync("call1");
        CompletableFuture<String> future2 = asyncTaskService.fetchDataAsync("call2");

        // Wait for both to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2);

        try {
            allFutures.get();
            String result1 = future1.get();
            String result2 = future2.get();

            log.info("All async calls completed");
            return "{\n  \"call1\": " + result1 + ",\n  \"call2\": " + result2 + "\n}";
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error waiting for async calls", e);
            return "Error waiting for async calls: " + e.getMessage();
        }
    }

    @GetMapping("/internal")
    public void internal(HttpServletRequest request) {
        log.info("In internal endpoint...");
    }
}

