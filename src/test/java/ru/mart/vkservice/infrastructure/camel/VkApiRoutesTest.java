package ru.mart.vkservice.infrastructure.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mart.vkservice.domain.model.VkUser;

import static org.junit.jupiter.api.Assertions.*;

class VkApiRoutesTest {

    private ObjectMapper objectMapper;
    private VkApiRoutes vkApiRoutes;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        vkApiRoutes = new VkApiRoutes();
    }

    @Test
    void processResponses_ShouldCombineUserAndGroupData() throws Exception {
        DefaultExchange exchange = new DefaultExchange(new DefaultCamelContext());

        String usersJson = """
                {
                    "response": [
                        {
                            "id": 12345,
                            "first_name": "Иван",
                            "last_name": "Иванов",
                            "middle_name": "Иванович"
                        }
                    ]
                }
                """;

        String groupsJson = """
                {
                    "response": 1
                }
                """;

        JsonNode usersResponse = objectMapper.readTree(usersJson);
        JsonNode groupsResponse = objectMapper.readTree(groupsJson);

        exchange.getIn().setBody(usersResponse);
        exchange.setProperty("groupsResponse", groupsResponse);

        vkApiRoutes.processResponses(exchange);

        VkUser result = exchange.getIn().getBody(VkUser.class);
        assertNotNull(result);
        assertEquals(12345L, result.getId());
        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        assertEquals("Иванович", result.getMiddleName());
        assertTrue(result.getIsMember());
    }

    @Test
    void processResponses_WhenNoMiddleName_ShouldHandleNull() throws Exception {
        DefaultExchange exchange = new DefaultExchange(new DefaultCamelContext());

        String usersJson = """
                {
                    "response": [
                        {
                            "id": 12345,
                            "first_name": "Петр",
                            "last_name": "Петров"
                        }
                    ]
                }
                """;

        String groupsJson = """
                {
                    "response": 0
                }
                """;

        JsonNode usersResponse = objectMapper.readTree(usersJson);
        JsonNode groupsResponse = objectMapper.readTree(groupsJson);

        exchange.getIn().setBody(usersResponse);
        exchange.setProperty("groupsResponse", groupsResponse);

        vkApiRoutes.processResponses(exchange);

        VkUser result = exchange.getIn().getBody(VkUser.class);
        assertNotNull(result);
        assertEquals("Петр", result.getFirstName());
        assertEquals("Петров", result.getLastName());
        assertNull(result.getMiddleName());
        assertFalse(result.getIsMember());
    }
}