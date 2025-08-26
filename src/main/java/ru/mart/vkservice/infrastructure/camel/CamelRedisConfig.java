package ru.mart.vkservice.infrastructure.camel;

import org.apache.camel.component.redis.RedisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class CamelRedisConfig {

    @Bean
    public RedisConfiguration redisConfiguration(RedisConnectionFactory redisConnectionFactory) {
        RedisConfiguration configuration = new RedisConfiguration();
        configuration.setConnectionFactory(redisConnectionFactory);
        return configuration;
    }
}
