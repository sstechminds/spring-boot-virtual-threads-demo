package com.web.opentelemetry.config;

import com.web.opentelemetry.component.RestClientBuilderFactory;
import com.web.opentelemetry.interceptor.TraceHttpInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

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
    public RestClient restClient(RestClientBuilderFactory restClientBuilderFactory) {
        if(this.virtualThreadsEnabled) {
            return restClientBuilderFactory.createWithVirtualThreads().build();
        } else {
//            return restClientBuilderFactory.create().build();
//            return restClientBuilderFactory
//                    .addTimeout(Duration.ofSeconds(10), Duration.ofSeconds(10))
//                    .defaultHeader("User-Agent", "Spring-Boot-App/1.0")
//                    .build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectionRequestTimeout(Duration.ofSeconds(10));
            factory.setReadTimeout(Duration.ofSeconds(10));
            return restClientBuilderFactory.create(builder ->
                    builder.requestFactory(factory) //add another factory
                            .defaultHeader("User-Agent", "Spring-Boot-App/1.0")
            ).build();
        }
    }
}
