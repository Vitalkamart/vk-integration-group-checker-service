package ru.mart.vkservice.infrastructure.adapter.output;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.infrastructure.exception.VkApiException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamelVkApiAdapterTest {

    @Mock
    private ProducerTemplate producerTemplate;

    @InjectMocks
    private CamelVkApiAdapter camelVkApiAdapter;

    @Test
    void getUserInfoWithMembership_ValidInput_ReturnsVkUser() {
        VkUser expectedUser = new VkUser(1L, "John", "Doe", null, true);
        when(producerTemplate.requestBodyAndHeader(anyString(), any(), anyString(), anyString(), eq(VkUser.class)))
                .thenReturn(expectedUser);

        VkUser result = camelVkApiAdapter.getUserInfoWithMembership(1L, 1L, "valid-token");

        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(producerTemplate).requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                any(),
                eq("vk_service_token"),
                eq("valid-token"),
                eq(VkUser.class)
        );
    }

    @Test
    void getUserInfoWithMembership_NullUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            camelVkApiAdapter.getUserInfoWithMembership(null, 1L, "token");
        });
    }

    @Test
    void getUserInfoWithMembership_InvalidUserId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            camelVkApiAdapter.getUserInfoWithMembership(0L, 1L, "token");
        });
    }

    @Test
    void getUserInfoWithMembership_NullToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            camelVkApiAdapter.getUserInfoWithMembership(1L, 1L, null);
        });
    }

    @Test
    void getUserInfoWithMembership_EmptyToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            camelVkApiAdapter.getUserInfoWithMembership(1L, 1L, "");
        });
    }

    @Test
    void getUserInfoWithMembership_VkApiException_ThrowsVkApiException() {
        VkApiException vkException = new VkApiException(100, "Test error");
        when(producerTemplate.requestBodyAndHeader(anyString(), any(), anyString(), anyString(), eq(VkUser.class)))
                .thenThrow(new RuntimeException(vkException));

        assertThrows(VkApiException.class, () -> {
            camelVkApiAdapter.getUserInfoWithMembership(1L, 1L, "token");
        });
    }
}