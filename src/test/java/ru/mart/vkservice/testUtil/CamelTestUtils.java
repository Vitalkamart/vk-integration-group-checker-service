package ru.mart.vkservice.testUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;

public class CamelTestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void mockVkApiCall(CamelContext camelContext, String routeId, String mockEndpointUri, String responseJson) throws Exception {
        AdviceWith.adviceWith(camelContext, routeId,
                advice -> advice.weaveByToUri("https://api.vk.com/method/*")
                        .replace()
                        .to(mockEndpointUri)
        );

        MockEndpoint mockEndpoint = camelContext.getEndpoint(mockEndpointUri, MockEndpoint.class);
        mockEndpoint.whenAnyExchangeReceived(exchange -> {
            JsonNode response = objectMapper.readTree(responseJson);
            exchange.getMessage().setBody(response);
        });
    }

    public static void mockVkApiCall(CamelContext camelContext, String routeId, String mockEndpointUri, JsonNode response) throws Exception {
        AdviceWith.adviceWith(camelContext, routeId,
                advice -> advice.weaveByToUri("https://api.vk.com/method/*")
                        .replace()
                        .to(mockEndpointUri)
        );

        MockEndpoint mockEndpoint = camelContext.getEndpoint(mockEndpointUri, MockEndpoint.class);
        mockEndpoint.whenAnyExchangeReceived(exchange -> {
            exchange.getMessage().setBody(response);
        });
    }
}
