package ru.mart.vkservice.infrastructure.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import ru.mart.vkservice.infrastructure.controller.dto.ErrorResponseDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    void handleResponseStatusException_ShouldReturnCorrectResponse() {
        when(webRequest.getDescription(anyBoolean())).thenReturn("uri=/test");
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test error");

        ResponseEntity<ErrorResponseDto> response = handler.handleResponseStatusException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test error", response.getBody().getMessage());
    }

    @Test
    void handleMissingRequestHeaderException_ShouldReturnCorrectResponse() {
        when(webRequest.getDescription(anyBoolean())).thenReturn("uri=/test");
        MissingRequestHeaderException ex = new MissingRequestHeaderException("vk_service_token", null);

        ResponseEntity<ErrorResponseDto> response = handler.handleMissingRequestHeaderException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("vk_service_token header is required", response.getBody().getMessage());
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() {
        when(webRequest.getDescription(anyBoolean())).thenReturn("uri=/test");
        Exception ex = new RuntimeException("Test exception");

        ResponseEntity<ErrorResponseDto> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}