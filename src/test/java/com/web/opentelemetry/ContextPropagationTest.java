package com.web.opentelemetry;

import com.web.opentelemetry.service.AsyncTaskService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
public class ContextPropagationTest {

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Test
    void testMdcPropagation() throws ExecutionException, InterruptedException {
        // 1. Set the context in the main/calling thread
        String expectedTraceId = "test-trace-123";
        MDC.put("requestId", expectedTraceId);

        try {
            // 2. Execute the async task
            CompletableFuture<String> futureResult = asyncTaskService.fetchDataAsync("callXYZ");

            // 3. Wait for the result and assert the propagated context
            String actualTraceId = futureResult.get(); // Blocks until completion
            assertFalse(actualTraceId.startsWith("null"));
            assertEquals(expectedTraceId, actualTraceId, "The traceId should be propagated to the async thread");

        } finally {
            // 4. Clean up MDC in the main thread
            MDC.remove("requestId");
        }
    }
}