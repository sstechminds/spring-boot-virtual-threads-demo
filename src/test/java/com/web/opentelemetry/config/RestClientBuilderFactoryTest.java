package com.web.opentelemetry.config;

import com.web.opentelemetry.component.RestClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RestClientBuilderFactoryTest {

    @Autowired
    private RestClientBuilderFactory restClientBuilderFactory;

    @Test
    void testCreateDefaultRestClient() {
        RestClient client = restClientBuilderFactory.create().build();
        assertNotNull(client, "RestClient should not be null");
    }

    @Test
    void testCreateRestClientWithBaseUrl() {
        String baseUrl = "https://api.example.com";
        RestClient client = restClientBuilderFactory.createWithBaseUrl(baseUrl).build();
        assertNotNull(client, "RestClient should not be null");
    }

    @Test
    void testCreateRestClientWithCustomHeaders() {
        RestClient client = restClientBuilderFactory.createWithHeaders(
                Map.of("Authorization", "Bearer token",
            "X-Custom-Header", "custom-value")
        ).build();
        assertNotNull(client, "RestClient should not be null");
    }

    @Test
    void testCreateRestClientWithCustomizer() {
        RestClient client = restClientBuilderFactory.create(builder ->
            builder.baseUrl("https://api.example.com")
                   .defaultHeader("X-Test", "test-value")
        ).build();
        assertNotNull(client, "RestClient should not be null");
    }

    @Test
    void testCreateRestClientWithCustomTimeout() {
        RestClient client = restClientBuilderFactory
                .addTimeout(Duration.ofSeconds(5), Duration.ofSeconds(30))
                .defaultHeader("User-Agent", "Spring-Boot-App/1.0")
                .build();
        assertNotNull(client, "RestClient should not be null");
    }

    @Test
    void testGetBuilder() {
        RestClient.Builder builder = restClientBuilderFactory.builder();
        assertNotNull(builder, "Builder should not be null");

        RestClient client = builder.build();
        assertNotNull(client, "RestClient built from factory builder should not be null");
    }
}

