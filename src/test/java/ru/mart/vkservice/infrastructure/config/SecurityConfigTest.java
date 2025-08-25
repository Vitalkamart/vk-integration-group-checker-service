package ru.mart.vkservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void securityFilterChain_WhenAuthenticated_ShouldAllowAccess() throws Exception {
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .contentType("application/json")
                        .content("{\"user_id\": 123, \"group_id\": 456}"))
                .andExpect(status().isBadRequest()); // 400 потому что нет заголовка vk_service_token
    }

    @Test
    void securityFilterChain_WhenNotAuthenticated_ShouldDenyAccess() throws Exception {
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .contentType("application/json")
                        .content("{\"user_id\": 123, \"group_id\": 456}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securityFilterChain_WhenApiDocs_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}