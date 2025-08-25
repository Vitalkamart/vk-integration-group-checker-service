package ru.mart.vkservice.infrastructure.adapter.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.mart.vkservice.infrastructure.config.VkApiConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VkApiAdapterTest {

    private static final String BASE_URL = "https://api.vk.com/method";
    private static final String VK_API_VERSION = "5.199";
    private static final Long USER_ID = 12345L;
    private static final Long GROUP_ID = 67890L;
    private static final String TEST_TOKEN = "test-token";

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private VkApiConfig vkApiConfig;

    @InjectMocks
    private VkApiAdapter vkApiAdapter;

    @BeforeEach
    void setUp() {
        when(vkApiConfig.getBaseUrl()).thenReturn(BASE_URL);
        when(vkApiConfig.getVersion()).thenReturn(VK_API_VERSION);

        ObjectMapper objectMapper = new ObjectMapper();
        vkApiAdapter = new VkApiAdapter(producerTemplate, vkApiConfig, objectMapper);
    }

    @Test
    void getUserInfo_ShouldReturnCorrectUser() throws Exception {
        // Arrange
        String validUserResponse = """
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

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(producerTemplate.requestBody(captor.capture(), any(), eq(String.class))).thenReturn(validUserResponse);

        // Act
        var result = vkApiAdapter.getUserInfo(USER_ID, TEST_TOKEN);
        String url = captor.getValue();

        // Assert
        assertTrue(url.contains("users.get"));
        assertTrue(url.contains("user_ids=12345"));
        assertTrue(url.contains("access_token=test-token"));
        assertTrue(url.contains("v=5.199"));

        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        assertEquals("Иванович", result.getMiddleName());
    }

    @Test
    void isGroupMember_WhenMember_ShouldReturnTrue() throws Exception {
        // Arrange
        String validMemberResponse = """
                {
                    "response": 1
                }
                """;

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(producerTemplate.requestBody(captor.capture(), any(), eq(String.class))).thenReturn(validMemberResponse);

        // Act
        boolean result = vkApiAdapter.isGroupMember(USER_ID, GROUP_ID, TEST_TOKEN);
        String url = captor.getValue();

        // Assert
        assertTrue(url.contains("groups.isMember"));
        assertTrue(url.contains("user_id=12345"));
        assertTrue(url.contains("group_id=67890"));
        assertTrue(url.contains("access_token=test-token"));

        assertTrue(result);
    }

    @Test
    void isGroupMember_WhenNotMember_ShouldReturnFalse() throws Exception {
        // Arrange
        String notMemberResponse = """
                {
                    "response": 0
                }
                """;

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(producerTemplate.requestBody(captor.capture(), any(), eq(String.class))).thenReturn(notMemberResponse);

        // Act
        boolean result = vkApiAdapter.isGroupMember(USER_ID, GROUP_ID, TEST_TOKEN);

        // Assert
        assertFalse(result);
    }

    @Test
    void getUserInfo_WhenVkApiError_ShouldThrowException() throws Exception {
        // Arrange
        String errorResponse = """
                {
                    "error": {
                        "error_code": 100,
                        "error_msg": "One of the parameters specified was missing or invalid: user_ids"
                    }
                }
                """;

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(producerTemplate.requestBody(captor.capture(), any(), eq(String.class))).thenReturn(errorResponse);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> vkApiAdapter.getUserInfo(USER_ID, TEST_TOKEN));

        assertTrue(exception.getMessage().contains("VK API Error 100"));
    }

    @Test
    void isGroupMember_WhenVkApiError_ShouldThrowException() throws Exception {
        // Arrange
        String errorResponse = """
                {
                    "error": {
                        "error_code": 203,
                        "error_msg": "Access to group denied"
                    }
                }
                """;

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(producerTemplate.requestBody(captor.capture(), any(), eq(String.class))).thenReturn(errorResponse);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> vkApiAdapter.isGroupMember(USER_ID, GROUP_ID, TEST_TOKEN));

        assertTrue(exception.getMessage().contains("VK API Error 203"));
    }

    @Test
    void getUserInfo_WhenNoMiddleName_ShouldHandleNull() throws Exception {
        // Arrange
        String responseWithoutMiddleName = """
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

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(producerTemplate.requestBody(captor.capture(), any(), eq(String.class))).thenReturn(responseWithoutMiddleName);

        // Act
        var result = vkApiAdapter.getUserInfo(USER_ID, TEST_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals("Петр", result.getFirstName());
        assertEquals("Петров", result.getLastName());
        assertNull(result.getMiddleName());
    }
}