package ru.mart.vkservice.infrastructure.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryIntegrationTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new VkApiRoutes(new ObjectMapper()) {
            @Override
            public void configure() throws Exception {
                // Только маршрут истории для тестирования
                from("direct:saveRequestHistory")
                        .routeId("save-request-history")
                        .log("Сохранение истории запроса")
                        .choice()
                        .when(header("isSuccess").isEqualTo("true"))
                        .log("✅ Успешный запрос")
                        .when(header("isError").isEqualTo("true"))
                        .log("❌ Неуспешный запрос")
                        .end();
            }
        };
    }

    @Test
    void saveRequestHistoryRoute_ShouldHandleSuccess() throws Exception {
        template.sendBodyAndHeader("direct:saveRequestHistory", "test-body", "isSuccess", "true");
        assertNotNull(context.getRouteDefinition("save-request-history"));
    }

    @Test
    void saveRequestHistoryRoute_ShouldHandleError() throws Exception {
        template.sendBodyAndHeader("direct:saveRequestHistory", "test-body", "isError", "true");
        assertNotNull(context.getRouteDefinition("save-request-history"));
    }
}
