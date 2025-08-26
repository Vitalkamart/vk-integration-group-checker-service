package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.domain.model.VkUser;

@Component
public class CacheResponseRoute extends RouteBuilder {

    private final ObjectMapper objectMapper;

    public CacheResponseRoute(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() throws Exception {
        from("direct:processCachedResponse")
                .routeId("cache-response-route")
                .log("Обработка данных из кэша: ${body}")
                .process(this::processCachedResponse);
    }

    private void processCachedResponse(Exchange exchange) {
        try {
            String cachedJson = exchange.getIn().getBody(String.class);
            JsonNode jsonNode = objectMapper.readTree(cachedJson);

            VkUser vkUser = new VkUser(
                    jsonNode.get("id").asLong(),
                    jsonNode.get("firstName").asText(),
                    jsonNode.get("lastName").asText(),
                    jsonNode.has("middleName") && !jsonNode.get("middleName").isNull() ?
                            jsonNode.get("middleName").asText() : null,
                    jsonNode.get("isMember").asBoolean()
            );

            exchange.getIn().setBody(vkUser);
            log.info("✅ Данные из кэша успешно обработаны: {}", vkUser);
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке данных из кэша: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process cached response", e);
        }
    }
}
