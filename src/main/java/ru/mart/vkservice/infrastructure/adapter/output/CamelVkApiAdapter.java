package ru.mart.vkservice.infrastructure.adapter.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.infrastructure.exception.VkApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CamelVkApiAdapter {

    private final ProducerTemplate producerTemplate;

    public VkUser getUserInfoWithMembership(Long userId, Long groupId, String serviceToken) {
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

            Throwable cause = e.getCause();
            if (cause instanceof VkApiException vkException) {
                throw vkException;
            }
            throw new RuntimeException("Ошибка при обращении к VK API: " + e.getMessage(), e);
        }
    }
}
