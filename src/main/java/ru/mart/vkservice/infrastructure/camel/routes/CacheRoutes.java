package ru.mart.vkservice.infrastructure.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheRoutes extends RouteBuilder {

    @Value("${cache.ttl.minutes:1}")
    private int cacheTtlMinutes;

    @Override
    public void configure() throws Exception {
        from("direct:getFromCache")
                .routeId("cache-get-route")
                .log("Проверка кэша для ключа: ${exchangeProperty.cacheKey}")
                .setHeader("CamelRedis.Command", constant("GET"))
                .setHeader("CamelRedis.Key", exchangeProperty("cacheKey"))
                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate");

        from("direct:saveToCache")
                .routeId("cache-save-route")
                .log("Сохранение в кэш с ключом: ${header.cacheKey}, TTL: ${header.cacheTtl} секунд")
                .setHeader("CamelRedis.Command", constant("SETEX"))
                .setHeader("CamelRedis.Key", header("cacheKey"))
                .setHeader("CamelRedis.Value", body())
                .setHeader("CamelRedis.Timeout", header("cacheTtl"))
                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate");

        from("direct:clearCacheByKey")
                .routeId("cache-clear-by-key-route")
                .log("Удаление из кэша по ключу: ${body}")
                .setHeader("CamelRedis.Command", constant("DEL"))
                .setHeader("CamelRedis.Key", body())
                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate");

        from("direct:clearAllCache")
                .routeId("cache-clear-all-route")
                .log("Очистка всего кэша")
                .setHeader("CamelRedis.Command", constant("FLUSHALL"))
                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate");
    }
}
