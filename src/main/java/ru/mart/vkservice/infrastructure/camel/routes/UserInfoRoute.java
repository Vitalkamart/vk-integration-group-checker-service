package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.infrastructure.camel.BaseVkRouteBuilder;
import ru.mart.vkservice.infrastructure.exception.VkApiException;

@Component
public class UserInfoRoute extends BaseVkRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:getUserInfo")
                .routeId("vk-user-info-route")
                .log("Получение информации о пользователе: ${body.userId}")

                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("https://api.vk.com/method/users.get?user_ids=${body.userId}&fields=first_name,last_name,middle_name&access_token=${exchangeProperty.serviceToken}&v=" + VK_API_VERSION)
                .unmarshal().json(JsonLibrary.Jackson, JsonNode.class)
                .process(this::processUserInfoResponse);
    }

    private void processUserInfoResponse(Exchange exchange) {
        JsonNode response = exchange.getIn().getBody(JsonNode.class);
        if (response.has("error")) {
            JsonNode error = response.get("error");
            throw new VkApiException(
                    error.get("error_code").asInt(),
                    error.get("error_msg").asText()
            );
        }

        JsonNode userArray = response.get("response");
        if (userArray == null || userArray.isEmpty()) {
            throw new RuntimeException("User not found in VK API response");
        }

        exchange.setProperty("userInfo", userArray.get(0));
    }
}
