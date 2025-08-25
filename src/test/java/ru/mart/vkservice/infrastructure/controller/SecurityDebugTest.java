package ru.mart.vkservice.infrastructure.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.mart.vkservice.infrastructure.config.TestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
class SecurityDebugTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debugSecurity() throws Exception {
        mockMvc.perform(post("/api/v1/vk-users/info")
                        .header("vk_service_token", "test-token")
                        .contentType("application/json")
                        .content("{\"user_id\": 12345, \"group_id\": 67890}"))
                .andExpect(status().isUnauthorized());
    }
}
