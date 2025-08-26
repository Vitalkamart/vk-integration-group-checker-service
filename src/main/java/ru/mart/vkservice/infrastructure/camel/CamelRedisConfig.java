package ru.mart.vkservice.infrastructure.camel;

import org.apache.camel.component.redis.RedisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CamelRedisConfig {

    @Bean
    public RedisConfiguration redisConfiguration(RedisConnectionFactory redisConnectionFactory) {
        RedisConfiguration configuration = new RedisConfiguration();
        configuration.setConnectionFactory(redisConnectionFactory);

        // Создаем отдельный RedisTemplate для Camel
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();

        configuration.setRedisTemplate(redisTemplate);
        return configuration;
    }
}
