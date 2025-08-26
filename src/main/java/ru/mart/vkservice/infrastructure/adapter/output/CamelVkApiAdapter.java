package ru.mart.vkservice.infrastructure.adapter.output;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.infrastructure.exception.VkApiException;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class CamelVkApiAdapter {

    private final ProducerTemplate producerTemplate;

    public VkUser getUserInfoWithMembership(
            @NotNull @Positive Long userId,
            @NotNull @Positive Long groupId,
            @NotNull String serviceToken) {
        validateInput(userId, groupId, serviceToken);
        log.debug("Calling VK API for user: {}, group: {}", userId, groupId);

        try {
            UserGroupRequest request = new UserGroupRequest(userId, groupId);
            return producerTemplate.requestBodyAndHeader(
                    "direct:getVkUserInfo",
                    request,
                    "vk_service_token",
                    serviceToken,
                    VkUser.class
            );
        } catch (Exception e) {
            log.error("VK API call failed for user: {}, group: {}", userId, groupId, e);
            throw extractVkException(e);
        }
    }

    private void validateInput(Long userId, Long groupId, String serviceToken) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (groupId == null || groupId <= 0) {
            throw new IllegalArgumentException("Group ID must be positive");
        }
        if (serviceToken == null || serviceToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Service token is required");
        }
    }

    private RuntimeException extractVkException(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof VkApiException vkException) {
            return vkException;
        }
        return new RuntimeException("Ошибка при обращении к VK API: " + e.getMessage(), e);
    }
}
