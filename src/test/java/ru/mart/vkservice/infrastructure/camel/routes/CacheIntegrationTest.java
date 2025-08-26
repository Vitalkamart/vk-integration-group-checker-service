package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mart.vkservice.infrastructure.adapter.output.UserGroupRequest;
import ru.mart.vkservice.infrastructure.config.TestContainersConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@Import(TestContainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CacheIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ProducerTemplate producerTemplate;

    @BeforeEach
    void setUp() throws Exception {
        AdviceWith.adviceWith(camelContext, "vk-user-info-route",
                advice -> advice.weaveByToUri("https://api.vk.com/method/*")
                        .replace()
                        .to("mock:vk-api-users")
        );

        AdviceWith.adviceWith(camelContext, "vk-group-membership-route",
                advice -> advice.weaveByToUri("https://api.vk.com/method/*")
                        .replace()
                        .to("mock:vk-api-groups")
        );

        AdviceWith.adviceWith(camelContext, "main-vk-user-info-route",
                advice -> {
                    advice.weaveByToUri("direct:getFromCache")
                            .replace()
                            .to("mock:cache-get");

                    advice.weaveByToUri("direct:saveToCache")
                            .replace()
                            .to("mock:cache-save");
                }
        );
    }

    @Test
    void getVkUserInfo_CacheFlow_WorksCorrectly() throws Exception {
        MockEndpoint userMock = camelContext.getEndpoint("mock:vk-api-users", MockEndpoint.class);
        MockEndpoint groupMock = camelContext.getEndpoint("mock:vk-api-groups", MockEndpoint.class);
        MockEndpoint cacheGetMock = camelContext.getEndpoint("mock:cache-get", MockEndpoint.class);
        MockEndpoint cacheSaveMock = camelContext.getEndpoint("mock:cache-save", MockEndpoint.class);

        String userResponse = """
                {"response": [{"id": 1, "first_name": "John", "last_name": "Doe"}]}
                """;
        String groupResponse = """
                {"response": 1}
                """;

        userMock.whenAnyExchangeReceived(exchange -> {
            JsonNode response = objectMapper.readTree(userResponse);
            exchange.getMessage().setBody(response);
        });

        groupMock.whenAnyExchangeReceived(exchange -> {
            JsonNode response = objectMapper.readTree(groupResponse);
            exchange.getMessage().setBody(response);
        });

        cacheGetMock.expectedMessageCount(1);
        cacheGetMock.whenAnyExchangeReceived(exchange -> {
            exchange.getMessage().setBody(null);
        });

        cacheSaveMock.expectedMessageCount(1);
        userMock.expectedMessageCount(1);
        groupMock.expectedMessageCount(1);

        Object result = producerTemplate.requestBodyAndHeader(
                "direct:getVkUserInfo",
                new UserGroupRequest(1L, 1L),
                "vk_service_token",
                "test-token"
        );

        assertNotNull(result);
        cacheGetMock.assertIsSatisfied();
        cacheSaveMock.assertIsSatisfied();
        userMock.assertIsSatisfied();
        groupMock.assertIsSatisfied();
    }

    @Test
    void getVkUserInfo_CacheHit_ReturnsCachedData() throws Exception {
        MockEndpoint cacheGetMock = camelContext.getEndpoint("mock:cache-get", MockEndpoint.class);
        MockEndpoint userMock = camelContext.getEndpoint("mock:vk-api-users", MockEndpoint.class);
        MockEndpoint groupMock = camelContext.getEndpoint("mock:vk-api-groups", MockEndpoint.class);

        String cachedResponse = """
                {
                    "id": 1,
                    "firstName": "John",
                    "lastName": "Doe",
                    "middleName": null,
                    "isMember": true
                }
                """;

        cacheGetMock.expectedMessageCount(1);
        cacheGetMock.whenAnyExchangeReceived(exchange -> {
            exchange.getMessage().setBody(cachedResponse);
        });

        userMock.expectedMessageCount(0);
        groupMock.expectedMessageCount(0);

        Object result = producerTemplate.requestBodyAndHeader(
                "direct:getVkUserInfo",
                new UserGroupRequest(1L, 1L),
                "vk_service_token",
                "test-token"
        );

        assertNotNull(result);
        cacheGetMock.assertIsSatisfied();
        userMock.assertIsSatisfied();
        groupMock.assertIsSatisfied();
    }
}