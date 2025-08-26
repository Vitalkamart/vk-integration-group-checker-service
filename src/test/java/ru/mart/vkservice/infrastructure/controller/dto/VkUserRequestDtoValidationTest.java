package ru.mart.vkservice.infrastructure.controller.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VkUserRequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validRequest_NoValidationErrors() {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(1L);
        request.setGroupId(1L);

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullUserId_HasValidationError() {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(null);
        request.setGroupId(1L);

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("user_id is required"));
    }

    @Test
    void zeroUserId_HasValidationError() {
        VkUserRequestDto request = new VkUserRequestDto();
        request.setUserId(0L);
        request.setGroupId(1L);

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("user_id must be positive"));
    }
}