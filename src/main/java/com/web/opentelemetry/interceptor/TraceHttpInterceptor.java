package com.web.opentelemetry.interceptor;

import org.slf4j.MDC;
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

        // Try to get requestId from MDC first (propagated from HttpServletRequest in controller)
        String mdcRequestId = MDC.get("requestId");

        // Use MDC values if available, otherwise check headers
        if (requestId == null) {
            if (mdcRequestId != null) {
                requestId = mdcRequestId;
            } else {
                // Generate new requestId (10 digits)
                requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            }
            request.getHeaders().add(HEADER_REQUEST_ID, requestId);
        }

        if (sessionId == null) {
            sessionId = requestId;
            request.getHeaders().set(HEADER_SESSION_ID, sessionId);
        }

        return execution.execute(request, body);
    }
}
