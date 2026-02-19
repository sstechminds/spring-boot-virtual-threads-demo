package com.web.opentelemetry.observability.logging;

import ch.qos.logback.access.tomcat.LogbackValve;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.tomcat.TomcatContextCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = {"org.apache.catalina.Context"})
public class TomcatAccessLogAutoConfig {

    /**
     * Setup embedded Tomcat logging using logback-access.xml file.
     */
    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return context -> {
            LogbackValve logbackValue = new LogbackValve();
            logbackValue.setFilename("logback-access.xml");
            logbackValue.setAsyncSupported(true);
            logbackValue.setQuiet(false);

            context.getPipeline().addValve(logbackValue);
            context.setUseRelativeRedirects(true); //enables  Tomcat contexts to support relative redirects
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

    @Bean
    public TomcatServletWebServerFactory servletContainer(TomcatContextCustomizer customizer) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addContextCustomizers(customizer);
        return factory;
    }
}
