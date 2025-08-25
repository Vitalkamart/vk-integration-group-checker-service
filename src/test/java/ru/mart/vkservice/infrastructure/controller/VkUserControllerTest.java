package ru.mart.vkservice.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.input.VkUserService;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserRequestDto;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VkUserController.class)
class VkUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VkUserService vkUserService;

    @Test
    void getUserInfo_ValidRequest_ShouldReturnUserInfo() throws Exception {
        // Arrange
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(12345L);
        request.setGroupId(67890L);

        VkUser mockUser = new VkUser(12345L, "Иван", "Иванов", "Иванович", true);
        when(vkUserService.getUserInfoWithMembership(anyLong(), anyLong(), anyString()))
                .thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Иванов"))
                .andExpect(jsonPath("$.middleName").value("Иванович"))
                .andExpect(jsonPath("$.member").value(true));

        verify(vkUserService).getUserInfoWithMembership(12345L, 67890L, "test-token");
    }

    @Test
    void getUserInfo_MissingHeader_ShouldReturnBadRequest() throws Exception {
        // Arrange
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(12345L);
        request.setGroupId(67890L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing Required Header"));
    }

    @Test
    void getUserInfo_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(-1L); // Invalid
        request.setGroupId(67890L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void getUserInfo_NullUserId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(null); // Invalid
        request.setGroupId(67890L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}