package ru.mart.vkservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = VkServiceApplication.class,
        properties = {
                "spring.main.web-application-type=none",
                "camel.springboot.main-run-controller=false"
        }
)
@ActiveProfiles("test")
class VkServiceApplicationContextTest {

    @Test
    @DisplayName("Check that application context loads successfully")
    void contextLoads() {
        // This test will pass if the application context loads without errors
    }
}