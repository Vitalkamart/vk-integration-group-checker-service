package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.infrastructure.exception.VkApiException;

@Component
public class GroupMembershipRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:checkGroupMembership")
                .routeId("vk-group-membership-route")
                .log("Проверка членства пользователя ${body.userId} в группе ${body.groupId}")

                .setHeader("CamelHttpMethod", constant("GET"))
                .toD("https://api.vk.com/method/groups.isMember?user_id=${body.userId}&group_id=${body.groupId}&access_token=${exchangeProperty.serviceToken}&v=5.199")
                .unmarshal().json(JsonLibrary.Jackson, JsonNode.class)
                .process(this::processMembershipResponse);
    }

    private void processMembershipResponse(Exchange exchange) {
        JsonNode response = exchange.getIn().getBody(JsonNode.class);
        boolean isMember = false;

        if (response.has("error")) {
            JsonNode error = response.get("error");
            int errorCode = error.get("error_code").asInt();
            // Ошибки 203 и 100 - нормальные ситуации
            if (errorCode != 203 && errorCode != 100) {
                throw new VkApiException(errorCode, error.get("error_msg").asText());
            }
        } else {
            isMember = response.get("response").asInt() == 1;
        }

        exchange.setProperty("isMember", isMember);
    }
}
