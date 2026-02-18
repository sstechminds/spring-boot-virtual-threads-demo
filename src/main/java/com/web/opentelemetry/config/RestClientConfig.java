package com.web.opentelemetry.config;

import am.ik.spring.http.client.RetryableClientHttpRequestInterceptor;
import com.web.opentelemetry.component.RestClientBuilderFactory;
import com.web.opentelemetry.interceptor.TraceHttpInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

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
            HttpComponentsClientHttpRequestFactory factory = clientHttpRequestFactory();
            return restClientBuilderFactory.create(builder ->
                    builder.requestFactory(factory) //add another factory

            ).build();
        }
    }

    @Bean("defaultPoolRestClient") //HttpComponentsClientHttpRequestFactory vs JdkClientHttpRequestFactory
    public RestClient restClient(RestClient.Builder builder) {
                return builder
                        .baseUrl("https://jsonplaceholder.typicode.com")
                        .build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            restClientBuilder
                    .defaultHeader("User-Agent", "Spring-Boot-App/1.0")
                    .requestFactory(clientHttpRequestFactory())
                    .requestInterceptor(new RetryableClientHttpRequestInterceptor(new FixedBackOff(100, 2)))
                    .configureMessageConverters(clientBuilder ->
                            clientBuilder.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper())));

        };
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    private static HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }
}
