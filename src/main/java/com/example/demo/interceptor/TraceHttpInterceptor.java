package com.example.demo.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.UUID;

public class TraceHttpInterceptor implements ClientHttpRequestInterceptor {
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_SESSION_ID = "X-Session-ID";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String requestId = request.getHeaders().getFirst(HEADER_REQUEST_ID);
        String sessionId = request.getHeaders().getFirst(HEADER_SESSION_ID);
        // Generate request/session IDs if missing
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            request.getHeaders().set(HEADER_REQUEST_ID, requestId);
        }
        if (sessionId == null) {
            sessionId = requestId;
            request.getHeaders().set(HEADER_SESSION_ID, sessionId);
        }
        return execution.execute(request, body);
    }
}
