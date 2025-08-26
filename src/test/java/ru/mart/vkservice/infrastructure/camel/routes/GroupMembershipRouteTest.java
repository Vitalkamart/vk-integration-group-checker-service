package ru.mart.vkservice.infrastructure.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mart.vkservice.infrastructure.adapter.output.UserGroupRequest;
import ru.mart.vkservice.infrastructure.config.TestContainersConfig;
import ru.mart.vkservice.infrastructure.exception.VkApiException;
import ru.mart.vkservice.testUtil.CamelTestUtils;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@Import(TestContainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GroupMembershipRouteTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    void checkGroupMembership_OtherError_ThrowsException() throws Exception {
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

        Exception exception = assertThrows(Exception.class, () -> {
            producerTemplate.requestBody("direct:checkGroupMembership", new UserGroupRequest(1L, 1L));
        });

        assertInstanceOf(VkApiException.class, exception.getCause());
        mockEndpoint.assertIsSatisfied();
    }
}