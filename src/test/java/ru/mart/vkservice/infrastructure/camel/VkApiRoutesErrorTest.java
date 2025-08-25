package ru.mart.vkservice.infrastructure.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VkApiRoutesErrorTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new VkApiRoutes() {
            @Override
            public void configure() throws Exception {
                super.configure();

                from("direct:testError")
                        .throwException(new RuntimeException("Test error"));
            }
        };
    }

    @Test
    void shouldHandleExceptions() throws Exception {
        assertThrows(Exception.class, () -> {
            template.sendBody("direct:testError", "test-body");
        });
    }
}
