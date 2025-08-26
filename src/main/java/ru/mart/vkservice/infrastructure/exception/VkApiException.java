package ru.mart.vkservice.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class VkApiException extends ResponseStatusException {
    private final Integer vkErrorCode;
    private final String vkErrorMessage;

    public VkApiException(Integer vkErrorCode, String vkErrorMessage) {
        super(HttpStatus.BAD_REQUEST, "VK API Error: " + vkErrorMessage);
        this.vkErrorCode = vkErrorCode;
        this.vkErrorMessage = vkErrorMessage;
    }
}
