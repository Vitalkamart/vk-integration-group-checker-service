package ru.mart.vkservice.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.output.VkApiPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VkUserServiceImplTest {
    private static final Long USER_ID = 12345L;
    public static final Long GROUP_ID = 67890L;

    @Mock
    private VkApiPort vkApiPort;

    @InjectMocks
    private VkUserServiceImpl vkUserService;

    @Test
    void getUserInfoWithMembership_ShouldReturnUserWithMembership() {
        // Arrange
        String token = "test-token";

        VkUser mockUser = new VkUser(USER_ID, "John", "Doe", "Middle", false);

        when(vkApiPort.getUserInfo(USER_ID, token)).thenReturn(mockUser);
        when(vkApiPort.isGroupMember(USER_ID, GROUP_ID, token)).thenReturn(true);

        // Act
        VkUser result = vkUserService.getUserInfoWithMembership(USER_ID, GROUP_ID, token);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Middle", result.getMiddleName());
        assertTrue(result.getIsMember());

        verify(vkApiPort).getUserInfo(USER_ID, token);
        verify(vkApiPort).isGroupMember(USER_ID, GROUP_ID, token);
    }

    @Test
    void getUserInfoWithMembership_WhenNotMember_ShouldReturnFalse() {
        // Arrange
        String token = "test-token";

        VkUser mockUser = new VkUser(USER_ID, "John", "Doe", "Middle", false);

        when(vkApiPort.getUserInfo(USER_ID, token)).thenReturn(mockUser);
        when(vkApiPort.isGroupMember(USER_ID, GROUP_ID, token)).thenReturn(false);

        // Act
        VkUser result = vkUserService.getUserInfoWithMembership(USER_ID, GROUP_ID, token);

        // Assert
        assertFalse(result.getIsMember());
    }
}