package com.web.opentelemetry.component;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Factory class for creating RestClient instances with default configuration.
 * Allows customization while maintaining consistent defaults across the application.
 */
@Component
public class RestClientBuilderFactory {
    private final ClientHttpRequestInterceptor traceHttpInterceptor;

    public RestClientBuilderFactory(ClientHttpRequestInterceptor traceHttpInterceptor) {
        this.traceHttpInterceptor = traceHttpInterceptor;
    }

    /**
     * Create a RestClient with default configuration
     */
    public RestClient.Builder create() {
        return builder();
    }

    /**
     * Create a RestClient with custom configuration
     * @param customizer Consumer to customize the builder
     */
    public RestClient.Builder create(Consumer<RestClient.Builder> customizer) {
        RestClient.Builder builder = builder();
        customizer.accept(builder);
        return builder;
    }

    /**
     * Get a builder with default configuration applied
     */
    public RestClient.Builder builder() {
        return RestClient.builder()
                .requestInterceptor(traceHttpInterceptor)
                .defaultHeader("Content-Type", "application/json");
    }

    /**
     * Create a RestClient with custom base URL
     */
    public RestClient.Builder createWithBaseUrl(String baseUrl) {
        return builder()
                .baseUrl(baseUrl);
    }

    /**
     * Create a RestClient with custom headers
     */
    public RestClient.Builder createWithHeaders(Map<?, ?> headers) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers is null");
        }

        RestClient.Builder builder = builder();
        for (Map.Entry<?, ?> entry : headers.entrySet()) {
            builder.defaultHeader(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return builder;
    }

    /**
     * Create a RestClient with additional interceptors
     */
    public RestClient.Builder createWithInterceptors(ClientHttpRequestInterceptor... additionalInterceptors) {
        RestClient.Builder builder = builder();
        for (ClientHttpRequestInterceptor interceptor : additionalInterceptors) {
            builder.requestInterceptor(interceptor);
        }
        return builder;
    }

    public RestClient.Builder createWithTimeout(Duration connectTimeout, Duration readTimeout, boolean virtualThreadsEnabled) {
        JdkClientHttpRequestFactory customFactory = clientHttpRequestFactory(connectTimeout, readTimeout, virtualThreadsEnabled);
        return builder().requestFactory(customFactory);
    }

    public RestClient.Builder createWithVirtualThreads() {
        JdkClientHttpRequestFactory customFactory = clientHttpRequestFactory(null, null, true);
        return builder().requestFactory(customFactory);
    }

    private JdkClientHttpRequestFactory clientHttpRequestFactory(Duration connectTimeout,
                                                                 Duration readTimeout,
                                                                 boolean virtualThreadsEnabled) {
        // Create HttpClient with virtual thread executor
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        if(virtualThreadsEnabled) {
            httpClientBuilder.connectTimeout(Duration.ofMillis(10000)).executor(Executors.newVirtualThreadPerTaskExecutor());
        }
        if(connectTimeout != null) {
            httpClientBuilder.connectTimeout(connectTimeout);
        }
        HttpClient httpClient = httpClientBuilder.connectTimeout(Duration.ofMillis(10000)).build();
        JdkClientHttpRequestFactory customFactory = new JdkClientHttpRequestFactory(httpClient);
        if(readTimeout != null) {
            customFactory.setReadTimeout(readTimeout);
        }
        return customFactory;
    }
}

