package com.web.opentelemetry.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.web.opentelemetry.observability.tracing.Constants.X_SSTECHMINDS_REQUEST_ID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(X_SSTECHMINDS_REQUEST_ID))
                .orElse(UUID.randomUUID().toString());

        MDC.put(X_SSTECHMINDS_REQUEST_ID, requestId);

        response.setHeader(X_SSTECHMINDS_REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(X_SSTECHMINDS_REQUEST_ID);
        }
    }
}
