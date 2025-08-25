package ru.mart.vkservice.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VkUserRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidRequest_thenNoViolations() {
        VkUserRequest request = new VkUserRequest();
        request.setUserId(12345L);
        request.setGroupId(67890L);

        Set<ConstraintViolation<VkUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUserIdIsNull_thenViolation() {
        VkUserRequest request = new VkUserRequest();
        request.setUserId(null);
        request.setGroupId(67890L);

        Set<ConstraintViolation<VkUserRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("user_id is required", violations.iterator().next().getMessage());
    }
}