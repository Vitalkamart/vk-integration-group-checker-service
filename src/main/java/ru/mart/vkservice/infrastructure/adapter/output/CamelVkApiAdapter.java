package ru.mart.vkservice.infrastructure.adapter.output;

import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.output.VkApiPort;

@Component
@RequiredArgsConstructor
public class CamelVkApiAdapter implements VkApiPort {

    private final ProducerTemplate producerTemplate;

    @Override
    public VkUser getUserInfo(Long userId, String serviceToken) {
        return producerTemplate.requestBodyAndHeader(
                "direct:getVkUserInfo",
                new UserGroupRequest(userId, null),
                "vk_service_token",
                serviceToken,
                VkUser.class
        );
    }

    @Override
    public boolean isGroupMember(Long userId, Long groupId, String serviceToken) {
        VkUser user = producerTemplate.requestBodyAndHeader(
                "direct:getVkUserInfo",
                new UserGroupRequest(userId, groupId),
                "vk_service_token",
                serviceToken,
                VkUser.class
        );
        return user.getIsMember();
    }

    private record UserGroupRequest(Long userId, Long groupId) {
    }
}
