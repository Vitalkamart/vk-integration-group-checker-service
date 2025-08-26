package ru.mart.vkservice.infrastructure.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MainVkApiRoute extends RouteBuilder {

    @Value("${cache.ttl.minutes:1}")
    private int cacheTtlMinutes;

    @Override
    public void configure() throws Exception {
        from("direct:getVkUserInfo")
                .routeId("main-vk-user-info-route")
                .errorHandler(noErrorHandler())

                .log("Обработка запроса: user=${body.userId}, group=${body.groupId}")

                // Генерируем ключ кэша
                .setProperty("cacheKey", simple("vk_user_${body.userId}_group_${body.groupId}"))

                // Сохраняем оригинальный запрос и токен
                .setProperty("originalRequest", body())
                .setProperty("serviceToken", header("vk_service_token"))

                // Пытаемся получить из кэша
                .to("direct:getFromCache")
                .choice()
                .when(body().isNotNull())
                .log("✅ Данные найдены в кэше")
                // Обрабатываем данные из кэша
                .to("direct:processCachedResponse")
                .stop() // Завершаем обработку
                .otherwise()
                .log("❌ Данных нет в кэше, запрос к VK API")
                // Восстанавливаем оригинальный запрос
                .setBody(exchangeProperty("originalRequest"))

                // Получаем информацию о пользователе
                .to("direct:getUserInfo")

                // Проверяем членство в группе
                .setBody(exchangeProperty("originalRequest"))
                .to("direct:checkGroupMembership")

                // Формируем финальный ответ
                .to("direct:buildResponse")

                // Сохраняем финальный ответ в property
                .setProperty("finalResponse", body())

                // Сохраняем в кэш (маршалим в JSON и преобразуем в строку)
                .marshal().json()
                .convertBodyTo(String.class)
                .log("Сохранение в кэш: ключ=${exchangeProperty.cacheKey}, значение=${body}")
                .setHeader("cacheKey", exchangeProperty("cacheKey"))
                .setHeader("cacheTtl", constant(cacheTtlMinutes * 60))
                .to("direct:saveToCache")

                // Возвращаем оригинальный ответ
                .setBody(exchangeProperty("finalResponse"))
                .log("✅ Данные получены из VK API и сохранены в кэш")
                .end()

                .log("Успешно обработан запрос");
    }
}
