package ru.mart.vkservice.infrastructure.camel;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.domain.model.VkUser;

@Component
@RequiredArgsConstructor
public class VkApiRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:getVkUserInfo")
                .routeId("vk-user-info-route")
                .onException(Exception.class)
                .handled(true)
                .log("VK API Error: ${exception.message}")
                .setHeader("isError", constant("true"))
                .to("direct:saveRequestHistory")
                .throwException(new RuntimeException("VK API error: ${exception.message}"))
                .end()

                .log("Начало обработки запроса для пользователя: ${body.userId}, группы: ${body.groupId}")

                .multicast()
                .to("direct:callVkUsersApi", "direct:callVkGroupsApi")
                .end()

                .process(this::processResponses)
                .log("Успешно обработан запрос: ${body}")

                .setHeader("isSuccess", constant("true")) // Устанавливаем заголовок
                .to("direct:saveRequestHistory")

                .to("log:vk-api-result?level=INFO&showAll=true");


        from("direct:callVkUsersApi")
                .routeId("vk-users-api-call")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("https://api.vk.com/method/users.get?user_ids=${body.userId}&fields=first_name,last_name,middle_name&access_token=${header.vk_service_token}&v=5.199")
                .unmarshal().json(JsonLibrary.Jackson, JsonNode.class)
                .log("Получен ответ от VK Users API");

        from("direct:callVkGroupsApi")
                .routeId("vk-groups-api-call")
                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("https://api.vk.com/method/groups.isMember?user_id=${body.userId}&group_id=${body.groupId}&access_token=${header.vk_service_token}&v=5.199")
                .unmarshal().json(JsonLibrary.Jackson, JsonNode.class)
                .setProperty("groupsResponse", body())
                .log("Получен ответ от VK Groups API");

        from("direct:saveRequestHistory")
                .routeId("save-request-history")
                .log("Сохранение истории запроса в PostgreSQL")
                .choice()
                .when(header("isSuccess").isEqualTo("true"))
                .log("✅ Успешный запрос - сохраняем в историю")
                //.to("jpa:RequestHistory?entityType=ru.mart.vkservice.domain.model.RequestHistory")
                .when(header("isError").isEqualTo("true"))
                .log("❌ Неуспешный запрос - сохраняем с ошибкой")
                //.to("jpa:RequestHistory?entityType=ru.mart.vkservice.domain.model.RequestHistory")
                .otherwise()
                .log("⚠️  Запрос без статуса - сохраняем как неизвестный")
                .end()
                .to("log:history-saved?level=INFO");
    }

    public void processResponses(Exchange exchange) {
        try {
            JsonNode usersResponse = exchange.getIn().getBody(JsonNode.class);
            JsonNode groupsResponse = exchange.getProperty("groupsResponse", JsonNode.class);

            JsonNode userData = usersResponse.get("response").get(0);
            boolean isMember = groupsResponse.get("response").asInt() == 1;

            VkUser vkUser = new VkUser(
                    userData.get("id").asLong(),
                    userData.get("first_name").asText(),
                    userData.get("last_name").asText(),
                    userData.has("middle_name") ? userData.get("middle_name").asText() : null,
                    isMember
            );

            exchange.getIn().setBody(vkUser);

        } catch (Exception e) {
            throw new RuntimeException("Error processing VK API responses", e);
        }
    }
}
