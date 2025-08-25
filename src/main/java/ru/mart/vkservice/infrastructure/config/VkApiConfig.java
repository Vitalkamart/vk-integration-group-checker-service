package ru.mart.vkservice.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vk.api")
public class VkApiConfig {
    private String baseUrl;
    private String version;
    private String serviceToken;
}
