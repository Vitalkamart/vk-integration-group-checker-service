package ru.mart.vkservice.infrastructure.camel.routes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.mart.vkservice.infrastructure.config.TestContainersConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Import(TestContainersConfig.class)
class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void redisConnection_Works() {
        String key = "test_key";
        String value = "test_value";

        redisTemplate.opsForValue().set(key, value);
        String result = redisTemplate.opsForValue().get(key);

        assertEquals(value, result);
    }

    @Test
    void redisCache_WithTTL_Works() throws InterruptedException {
        String key = "ttl_test";
        String value = "temp_value";

        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, java.time.Duration.ofSeconds(1));

        assertNotNull(redisTemplate.opsForValue().get(key));

        Thread.sleep(1100);
        assertNull(redisTemplate.opsForValue().get(key));
    }
}
