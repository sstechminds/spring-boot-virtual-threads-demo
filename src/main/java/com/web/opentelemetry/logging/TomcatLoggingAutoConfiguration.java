package com.web.opentelemetry.logging;

import ch.qos.logback.access.tomcat.LogbackValve;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = {"org.apache.catalina.Context"})
public class TomcatLoggingAutoConfiguration {

    /**
     * Setup embedded Tomcat logging using logback-access.xml file.
     */
    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return context -> {
            LogbackValve logbackValue = new LogbackValve();
            logbackValue.setFilename("logback-access.xml");
            logbackValue.setQuiet(false);
            context.getPipeline().addValve(logbackValue);
        };
    }

//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogsCustomizer() {
//        return factory -> {
//            var logbackValve = new LogbackValve();
//            logbackValve.setFilename("logback-access.xml");
//            logbackValve.setQuiet(false);
//            logbackValve.setAsyncSupported(true);
//            factory.addContextValves(logbackValve);
//        };
//    }
}
