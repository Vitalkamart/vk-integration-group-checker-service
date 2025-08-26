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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GroupMembershipRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void checkGroupMembership_OtherError_ThrowsException() throws Exception {
        // Arrange
        String jsonResponse = """
                {
                    "error": {
                        "error_code": 500,
                        "error_msg": "Server error"
                    }
                }
                """;

        CamelTestUtils.mockVkApiCall(camelContext, "vk-group-membership-route", "mock:vk-api-groups", jsonResponse);
        MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:vk-api-groups", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            producerTemplate.requestBody("direct:checkGroupMembership", new UserGroupRequest(1L, 1L));
        });

        assertTrue(exception.getCause() instanceof VkApiException);
        mockEndpoint.assertIsSatisfied();
    }
}