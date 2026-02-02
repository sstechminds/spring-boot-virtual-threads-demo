package com.web.opentelemetry.observability.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.web.opentelemetry.observability.tracing.Constants.X_SSTECHMINDS_REQUEST_ID;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceHeaderFilter extends OncePerRequestFilter {
    private final String hostId;

    public TraceHeaderFilter(String hostId) {
        this.hostId = hostId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestId= request.getHeader(X_SSTECHMINDS_REQUEST_ID);
        if(!StringUtils.hasText(requestId)) {
           requestId = UUID.randomUUID().toString();
        }

        // Log request details before processing the request.
        log.info("Request: Method={}, URI={}, Headers={}",
                request.getMethod(),
                request.getRequestURI(), getRequestHeaders(request));

        //This wrapper holds trace headers as additional request headers to the rest of the filter chain!
        TraceHeaderRequestWrapper requestWrapper = new TraceHeaderRequestWrapper(request, hostId, requestId);

        filterChain.doFilter(requestWrapper, response);
    }

    // Utility method to extract request headers for logging.
    private String getRequestHeaders(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        request.getHeaderNames().asIterator()
                .forEachRemaining(header -> sb.append(header).append("=").append(request.getHeader(header)).append(","));
        return sb.toString();
    }
}
