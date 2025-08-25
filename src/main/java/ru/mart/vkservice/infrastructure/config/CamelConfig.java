package ru.mart.vkservice.infrastructure.config;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Bean
    public CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                // Configure Camel context if needed
            }

            @Override
            public void afterApplicationStart(CamelContext context) {
                // Post-start configuration
            }
        };
    }
}
