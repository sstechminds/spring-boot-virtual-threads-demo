package com.web.opentelemetry.config;

import com.web.opentelemetry.interceptor.TraceHttpInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

@Configuration
public class RestClientConfig {
    private final boolean virtualThreadsEnabled;

    public RestClientConfig(@Value("${spring.threads.virtual.enabled:true}") String virtualThreadsEnabled) {
        this.virtualThreadsEnabled = Boolean.parseBoolean(virtualThreadsEnabled);
    }

    @Bean
    public ClientHttpRequestInterceptor traceHttpInterceptor() {
        return new TraceHttpInterceptor();
    }

    @Bean
    public JdkClientHttpRequestFactory clientHttpRequestFactory() {
        // Create HttpClient with virtual thread executor
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        if(this.virtualThreadsEnabled) {
            httpClientBuilder.executor(Executors.newVirtualThreadPerTaskExecutor());
        }
        httpClientBuilder.connectTimeout(Duration.ofSeconds(10));
        HttpClient httpClient = httpClientBuilder.build();
        return new JdkClientHttpRequestFactory(httpClient);
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder,
                                 JdkClientHttpRequestFactory clientHttpRequestFactory,
                                 ClientHttpRequestInterceptor traceHttpInterceptor) {
        return builder
                .requestFactory(clientHttpRequestFactory)
                .requestInterceptor(traceHttpInterceptor)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
