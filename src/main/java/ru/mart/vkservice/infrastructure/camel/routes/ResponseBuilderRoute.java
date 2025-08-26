package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.domain.model.VkUser;

@Component
public class ResponseBuilderRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:buildResponse")
                .routeId("response-builder-route")
                .log("Начало построения ответа из кэша")
                .process(this::buildVkUserResponse);
    }

    private void buildVkUserResponse(Exchange exchange) {
        try {
            JsonNode userInfo = exchange.getProperty("userInfo", JsonNode.class);
            boolean isMember = exchange.getProperty("isMember", Boolean.class);

            VkUser vkUser = new VkUser(
                    userInfo.get("id").asLong(),
                    userInfo.get("first_name").asText(),
                    userInfo.get("last_name").asText(),
                    userInfo.has("middle_name") && !userInfo.get("middle_name").isNull() ?
                            userInfo.get("middle_name").asText() : null,
                    isMember
            );

            exchange.getIn().setBody(vkUser);
            log.info("✅ Ответ успешно построен: {}", vkUser);
        } catch (Exception e) {
            log.error("❌ Ошибка при построении ответа: {}", e.getMessage(), e);
            throw e;
        }
    }
}
