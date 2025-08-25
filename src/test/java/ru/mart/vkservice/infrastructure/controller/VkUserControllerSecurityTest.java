package ru.mart.vkservice.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.input.VkUserService;
import ru.mart.vkservice.infrastructure.config.TestConfig;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserRequestDto;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
class VkUserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VkUserService vkUserService;

    @Test
    @WithMockUser(roles = "USER")
    void getUserInfo_WithMockUser_ShouldReturnOk() throws Exception {
        var request = createValidRequest();
        var mockUser = new VkUser(12345L, "Иван", "Иванов", "Иванович", true);

        when(vkUserService.getUserInfoWithMembership(anyLong(), anyLong(), anyString()))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserInfo_WithValidBasicAuth_ShouldReturnOk() throws Exception {
        var request = createValidRequest();
        var mockUser = new VkUser(12345L, "Иван", "Иванов", "Иванович", true);

        when(vkUserService.getUserInfoWithMembership(anyLong(), anyLong(), anyString()))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .with(httpBasic("admin", "password"))
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserInfo_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        var request = createValidRequest();

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserInfo_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        var request = createValidRequest();

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .with(httpBasic("wrong-user", "wrong-password"))
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private VkUserRequestDto createValidRequest() {
        var request = new VkUserRequestDto();
        request.setUserId(12345L);
        request.setGroupId(67890L);
        return request;
    }
}