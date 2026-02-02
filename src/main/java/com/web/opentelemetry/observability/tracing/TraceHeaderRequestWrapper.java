package com.web.opentelemetry.observability.tracing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;
import java.util.stream.Collectors;

import static com.web.opentelemetry.observability.tracing.Constants.X_SSTECHMINDS_HOST_ID;
import static com.web.opentelemetry.observability.tracing.Constants.X_SSTECHMINDS_REQUEST_ID;

public class TraceHeaderRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> additionalHeaders;

    public TraceHeaderRequestWrapper(HttpServletRequest request, String hostId, String requestId) {
        super(request);

        Map<String, String> headers = new HashMap<>();
        headers.put(X_SSTECHMINDS_HOST_ID, hostId);
        headers.put(X_SSTECHMINDS_REQUEST_ID, requestId);

        this.additionalHeaders = headers;
    }

    @Override
    public String getHeader(String name) {
        for(Map.Entry<String, String> e: additionalHeaders.entrySet()) {
            if(e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>();
        Enumeration<String> headerNames = super.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            names.add(headerNames.nextElement());
        }
        names.addAll(additionalHeaders.keySet());
        Set<String> seenLowerCase = new HashSet<>();
        return Collections.enumeration(names.stream()
                .filter(name -> !seenLowerCase.add(name.toLowerCase())) // add returns false if already present
                .collect(Collectors.toList()));
    }
}
