# RestClient Factory

The `RestClientFactory` provides a centralized way to create `RestClient` instances with consistent default configuration while allowing customization when needed.

## Default Configuration

The factory automatically applies:
- Virtual threads executor (when enabled)
- Trace HTTP interceptor for request ID propagation
- Default Content-Type header: `application/json`
- Connection timeout: 10 seconds

## Usage Examples

### 1. Basic Usage - Default Configuration

```java
@Autowired
private RestClientFactory restClientFactory;

public void example() {
    RestClient client = restClientFactory.create();
    String response = client.get()
        .uri("https://api.example.com/data")
        .retrieve()
        .body(String.class);
}
```

### 2. Custom Configuration with Builder

```java
RestClient client = restClientFactory.create(builder -> 
    builder
        .baseUrl("https://api.example.com")
        .defaultHeader("Authorization", "Bearer token")
);

String response = client.get()
    .uri("/data")
    .retrieve()
    .body(String.class);
```

### 3. Create with Base URL

```java
RestClient client = restClientFactory.createWithBaseUrl("https://api.example.com");

String response = client.get()
    .uri("/users")
    .retrieve()
    .body(String.class);
```

### 4. Create with Custom Headers

```java
RestClient client = restClientFactory.createWithHeaders(
    "Authorization", "Bearer token",
    "X-API-Key", "my-api-key"
);
```

### 5. Create with Additional Interceptors

```java
ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
    logger.info("Request: {} {}", request.getMethod(), request.getURI());
    return execution.execute(request, body);
};

RestClient client = restClientFactory.createWithInterceptors(loggingInterceptor);
```

### 6. Create with Custom Timeout

```java
RestClient client = restClientFactory.createWithTimeout(
    Duration.ofSeconds(5),   // connect timeout
    Duration.ofSeconds(30)   // read timeout
);
```

### 7. Get Builder for Advanced Customization

```java
RestClient client = restClientFactory.builder()
    .baseUrl("https://api.example.com")
    .defaultHeader("Accept", "application/json")
    .defaultStatusHandler(/* custom status handler */)
    .build();
```

## Benefits

1. **Consistency**: All RestClient instances share the same base configuration
2. **Maintainability**: Changes to default configuration are centralized
3. **Flexibility**: Easy to customize individual clients when needed
4. **Testing**: Easy to mock or replace the factory in tests
5. **Virtual Threads**: Automatically uses virtual threads when enabled
6. **Tracing**: All clients automatically include request ID propagation

## Migration from Direct Bean Injection

### Before:
```java
@Autowired
RestClient restClient;

String response = restClient.get()
    .uri("https://api.example.com/data")
    .retrieve()
    .body(String.class);
```

### After (using factory):
```java
@Autowired
RestClientFactory restClientFactory;

String response = restClientFactory.create().get()
    .uri("https://api.example.com/data")
    .retrieve()
    .body(String.class);

// Or create once and reuse:
private final RestClient restClient;

public MyService(RestClientFactory factory) {
    this.restClient = factory.create();
}
```

## Thread Safety

The factory itself is thread-safe and can be injected as a singleton. Each `RestClient` instance created by the factory is also thread-safe and can be reused across multiple threads.

## Virtual Threads

When `spring.threads.virtual.enabled=true`, all RestClient instances created by this factory will use virtual threads for HTTP requests, improving scalability and resource utilization.

