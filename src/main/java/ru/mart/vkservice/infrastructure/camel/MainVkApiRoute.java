package ru.mart.vkservice.infrastructure.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MainVkApiRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:getVkUserInfo")
                .routeId("main-vk-user-info-route")
                .errorHandler(noErrorHandler())

                .log("Обработка запроса: user=${body.userId}, group=${body.groupId}")

                .setProperty("originalRequest", body())
                .setProperty("serviceToken", header("vk_service_token"))

                .to("direct:getUserInfo")

                .setBody(exchangeProperty("originalRequest"))
                .to("direct:checkGroupMembership")

                .to("direct:buildResponse")

                .log("Успешно обработан запрос для пользователя: ${body.firstName} ${body.lastName}");
    }
}
