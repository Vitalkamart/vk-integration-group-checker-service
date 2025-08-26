package ru.mart.vkservice.infrastructure.camel.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mart.vkservice.infrastructure.config.TestContainersConfig;

import java.util.Map;

@SpringBootTest
@Testcontainers
@Import(TestContainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CacheRoutesTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @BeforeEach
    void setUp() throws Exception {
        AdviceWith.adviceWith(camelContext, "cache-get-route",
                advice -> advice.weaveByToUri("spring-redis://*")
                        .replace()
                        .to("mock:redis")
        );

        AdviceWith.adviceWith(camelContext, "cache-save-route",
                advice -> advice.weaveByToUri("spring-redis://*")
                        .replace()
                        .to("mock:redis")
        );
    }

    @Test
    void getFromCache_SendsCorrectCommand() throws Exception {
        MockEndpoint redisMock = camelContext.getEndpoint("mock:redis", MockEndpoint.class);
        redisMock.expectedMessageCount(1);

        producerTemplate.sendBodyAndHeader("direct:getFromCache",
                null, "cacheKey", "test_key");

        redisMock.assertIsSatisfied();
    }

    @Test
    void saveToCache_SendsCorrectCommand() throws Exception {
        MockEndpoint redisMock = camelContext.getEndpoint("mock:redis", MockEndpoint.class);
        redisMock.expectedMessageCount(1);

        producerTemplate.sendBodyAndHeaders("direct:saveToCache",
                "test_value",
                Map.of(
                        "cacheKey", "test_key",
                        "cacheTtl", 60
                )
        );

        redisMock.assertIsSatisfied();
    }
}