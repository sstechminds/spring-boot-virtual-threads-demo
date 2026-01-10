package com.web.opentelemetry.service;

import com.web.opentelemetry.component.RestClientBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

/**
 * Example service demonstrating various ways to use RestClientFactory.
 * This shows how to create customized RestClient instances for different use cases.
 */
@Service
public class ExampleRestClientUsageService {

    private static final Logger logger = LoggerFactory.getLogger(ExampleRestClientUsageService.class);

    private final RestClient defaultClient;
    private final RestClient apiClient;
    private final RestClient externalServiceClient;
    private final RestClientBuilderFactory restClientBuilderFactory;

    public ExampleRestClientUsageService(RestClientBuilderFactory restClientBuilderFactory) {
        this.restClientBuilderFactory = restClientBuilderFactory;

        // Example 1: Create a default client (reuse across methods)
        this.defaultClient = restClientBuilderFactory.create().build();

        // Example 2: Create a client with base URL for internal API
        this.apiClient = restClientBuilderFactory.createWithBaseUrl("http://localhost:8080/api").build();

        // Example 3: Create a client with custom configuration for external service
        this.externalServiceClient = restClientBuilderFactory.create(builder ->
            builder
                    //.requestFactory(null)
                    .baseUrl("https://dog.ceo/api")
                    .defaultHeader("User-Agent", "Spring-Boot-App/1.0")
        ).build();
    }

    /**
     * Example using the default client
     */
    public String fetchWithDefaultClient(String url) {
        return defaultClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

    /**
     * Example using the API client with base URL
     */
    public String fetchFromInternalApi(String endpoint) {
        return apiClient.get()
                .uri(endpoint) // URL is relative to base URL
                .retrieve()
                .body(String.class);
    }

    /**
     * Example using the external service client
     */
    public String fetchRandomDogImage() {
        return externalServiceClient.get()
                .uri("/breeds/image/random")
                .retrieve()
                .body(String.class);
    }

    /**
     * Example creating a one-time client with custom timeout
     */
    public String fetchWithCustomTimeout(String url) {
        RestClient client = restClientBuilderFactory.addTimeout(
            Duration.ofSeconds(2),   // Quick connect timeout
            Duration.ofSeconds(10)   // Read timeout
        ).build();

        return client.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

    /**
     * Example creating a client with authentication headers
     */
    public String fetchWithAuth(String url, String token) {
        RestClient authClient = restClientBuilderFactory.createWithHeaders(
            Map.of("Authorization", "Bearer " + token,
            "Accept", "application/json")
        ).build();

        return authClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

    /**
     * Example creating a client with custom interceptor
     */
    public String fetchWithLogging(String url) {
        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            logger.info("Making request to: {} {}", request.getMethod(), request.getURI());
            var response = execution.execute(request, body);
            logger.info("Response status: {}", response.getStatusCode());
            return response;
        };

        RestClient client = restClientBuilderFactory.addInterceptors(loggingInterceptor).build();

        return client.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

    /**
     * Example creating a highly customized client
     */
    public String fetchWithComplexConfig(String url) {
        RestClient client = restClientBuilderFactory.create(builder ->
            builder
                .baseUrl("https://api.example.com")
                .defaultHeader("X-Custom-Header", "custom-value")
                .defaultHeader("Accept", "application/json")
                .defaultStatusHandler(status -> status.is4xxClientError(),
                    (request, response) -> {
                        logger.error("Client error: {}", response.getStatusCode());
                    })
        ).build();

        return client.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }

    /**
     * Example getting a builder for maximum flexibility
     */
    public String fetchWithMaximumCustomization(String url) {
        RestClient client = restClientBuilderFactory.builder()
                .baseUrl("https://api.example.com")
                .defaultHeader("X-API-Version", "v2")
                // Add more customizations as needed
                .build();

        return client.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }
}

