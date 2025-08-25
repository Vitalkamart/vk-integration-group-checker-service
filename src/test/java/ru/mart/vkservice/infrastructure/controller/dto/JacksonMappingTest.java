package ru.mart.vkservice.infrastructure.controller.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JacksonMappingTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void shouldMapSnakeCaseJsonToCamelCaseFields() throws Exception {
        String json = """
                {
                  "user_id": 12345,
                  "group_id": 67890
                }
                """;

        VkUserRequestDto dto = objectMapper.readValue(json, VkUserRequestDto.class);

        assertNotNull(dto);
        assertEquals(12345L, dto.getUserId());
        assertEquals(67890L, dto.getGroupId());
    }

    @Test
    void shouldMapCamelCaseToSnakeCaseJson() throws Exception {
        VkUserResponseDto response = new VkUserResponseDto(
                "Иванов", "Иван", "Иванович", true
        );

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"last_name\":\"Иванов\""));
        assertTrue(json.contains("\"first_name\":\"Иван\""));
        assertTrue(json.contains("\"middle_name\":\"Иванович\""));
        assertTrue(json.contains("\"member\":true"));
    }
}
