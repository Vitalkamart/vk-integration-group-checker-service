package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.mart.vkservice.infrastructure.adapter.output.UserGroupRequest;
import ru.mart.vkservice.infrastructure.exception.VkApiException;
import ru.mart.vkservice.testUtil.CamelTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserInfoRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserInfo_ValidResponse_ProcessesSuccessfully() throws Exception {
        String jsonResponse = """
                {
                    "response": [
                        {
                            "id": 1,
                            "first_name": "John",
                            "last_name": "Doe"
                        }
                    ]
                }
                """;

        CamelTestUtils.mockVkApiCall(camelContext, "vk-user-info-route", "mock:vk-api-users", jsonResponse);
        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:vk-api-users", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);

        Object result = producerTemplate.requestBody("direct:getUserInfo", new UserGroupRequest(1L, 1L));

        assertNotNull(result);
        mockEndpoint.assertIsSatisfied();
    }

    @Test
    void getUserInfo_ErrorResponse_ThrowsVkApiException() throws Exception {
        String jsonResponse = """
                {
                    "error": {
                        "error_code": 100,
                        "error_msg": "User not found"
                    }
                }
                """;

        CamelTestUtils.mockVkApiCall(camelContext, "vk-user-info-route", "mock:vk-api-users", jsonResponse);
        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:vk-api-users", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);

        Exception exception = assertThrows(Exception.class, () -> {
            producerTemplate.requestBody("direct:getUserInfo", new UserGroupRequest(1L, 1L));
        });

        assertInstanceOf(VkApiException.class, exception.getCause());
        mockEndpoint.assertIsSatisfied();
    }
}