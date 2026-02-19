package com.web.opentelemetry.observability.tracing;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@AutoConfiguration
public class TracingAutoConfig {

    @Bean //SB4 context propagation. But, to support Virtual threads refer AsyncThreadConfig.java
    ContextPropagatingTaskDecorator taskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(name = "hostId")
    public String hostId() {
        String host = System.getenv("HOSTNAME");
        if(!StringUtils.hasText(host)) {
           try {
               host = InetAddress.getLocalHost().getHostName();
           } catch (UnknownHostException ex) {
               return "";
           }
        }
        return host.toLowerCase();
    }

    @Bean
    @ConditionalOnMissingBean(name ="traceHeaderFiler")
    public TraceHeaderFilter traceHeaderFilter(String hostId) {
        return new TraceHeaderFilter(hostId);
    }

}
