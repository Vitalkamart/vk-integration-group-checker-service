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
                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate")
                // Детальное логирование того, что вернулось из Redis
                .log("Результат из Redis: body=[${body}], body.class=[${body.class}]")
                .choice()
                .when(simple("${body} == null"))
                .log("Кэш пустой, возвращаем null")
                .setBody(simple("${null}"))
                .when(simple("${body.class.name} contains 'UserGroupRequest'"))
                .log("⚠️ В кэше найден UserGroupRequest объект вместо JSON - это ошибка, возвращаем null")
                .setBody(simple("${null}"))
                .otherwise()
                .log("Данные найдены в кэше: ${body}")
                // Преобразуем в String (данные из Redis)
                .convertBodyTo(String.class)
                .end();

        from("direct:saveToCache")
                .routeId("cache-save-route")
                .log("Сохранение в кэш с ключом: ${header.cacheKey}, TTL: ${header.cacheTtl} секунд")
                // Убедимся, что body это String (JSON)
                .convertBodyTo(String.class)
                .log("Сохраняемый JSON: ${body}")
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
