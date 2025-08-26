package ru.mart.vkservice.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.mart.vkservice.config.TestSecurityConfig;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.infrastructure.adapter.output.CamelVkApiAdapter;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserRequestDto;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VkUserController.class)
@Import(TestSecurityConfig.class)
class VkUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CamelVkApiAdapter vkApiAdapter;

    @Test
    @WithMockUser(username = "admin", password = "password", roles = "USER")
    void getUserInfo_ValidRequest_ReturnsUserInfo() throws Exception {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(1L);
        request.setGroupId(1L);

        VkUser user = new VkUser(1L, "John", "Doe", "Smith", true);
        when(vkApiAdapter.getUserInfoWithMembership(anyLong(), anyLong(), anyString()))
                .thenReturn(user);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("John"))
                .andExpect(jsonPath("$.last_name").value("Doe"))
                .andExpect(jsonPath("$.middle_name").value("Smith"))
                .andExpect(jsonPath("$.member").value(true));
    }

    @Test
    @WithMockUser(username = "admin", password = "password", roles = "USER")
    void getUserInfo_InvalidRequest_ReturnsBadRequest() throws Exception {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(0L); // Invalid
        request.setGroupId(1L);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserInfo_Unauthorized_ReturnsUnauthorized() throws Exception {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(1L);
        request.setGroupId(1L);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}