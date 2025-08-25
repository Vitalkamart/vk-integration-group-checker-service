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
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.input.VkUserService;
import ru.mart.vkservice.infrastructure.config.TestConfig;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserRequestDto;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = VkUserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        }
)
@Import(TestConfig.class)
class VkUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VkUserService vkUserService;

    @Test
    @WithMockUser(roles = "USER")
    void getUserInfo_ValidRequest_ShouldReturnUserInfo() throws Exception {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(12345L);
        request.setGroupId(67890L);

        VkUser mockUser = new VkUser(12345L, "Иван", "Иванов", "Иванович", true);
        when(vkUserService.getUserInfoWithMembership(anyLong(), anyLong(), anyString()))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_name").value("Иванов"))
                .andExpect(jsonPath("$.first_name").value("Иван"))
                .andExpect(jsonPath("$.middle_name").value("Иванович"))
                .andExpect(jsonPath("$.member").value(true));

        verify(vkUserService).getUserInfoWithMembership(12345L, 67890L, "test-token");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserInfo_MissingHeader_ShouldReturnBadRequest() throws Exception {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(12345L);
        request.setGroupId(67890L);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing Required Header"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserInfo_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(-1L);
        request.setGroupId(67890L);

        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}