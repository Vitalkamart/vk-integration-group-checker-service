package ru.mart.vkservice.infrastructure.adapter.output;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mart.vkservice.domain.model.VkUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CamelVkApiAdapterTest {

    @Mock
    private ProducerTemplate producerTemplate;

    @InjectMocks
    private CamelVkApiAdapter camelVkApiAdapter;

    @Test
    void getUserInfo_ShouldReturnUser() {
        VkUser expectedUser = new VkUser(12345L, "Иван", "Иванов", "Иванович", true);
        when(producerTemplate.requestBodyAndHeader(anyString(), any(), anyString(), anyString(), eq(VkUser.class)))
                .thenReturn(expectedUser);

        VkUser result = camelVkApiAdapter.getUserInfo(12345L, "test-token");

        assertNotNull(result);
        assertEquals("Иван", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        verify(producerTemplate).requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                any(),
                eq("vk_service_token"),
                eq("test-token"),
                eq(VkUser.class)
        );
    }

    @Test
    void isGroupMember_ShouldReturnMembershipStatus() {
        VkUser userWithMembership = new VkUser(12345L, "Иван", "Иванов", "Иванович", true);
        when(producerTemplate.requestBodyAndHeader(anyString(), any(), anyString(), anyString(), eq(VkUser.class)))
                .thenReturn(userWithMembership);

        boolean result = camelVkApiAdapter.isGroupMember(12345L, 67890L, "test-token");

        assertTrue(result);
        verify(producerTemplate).requestBodyAndHeader(
                eq("direct:getVkUserInfo"),
                any(),
                eq("vk_service_token"),
                eq("test-token"),
                eq(VkUser.class)
        );
    }
}