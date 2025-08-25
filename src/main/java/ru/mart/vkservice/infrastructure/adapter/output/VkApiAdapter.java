package ru.mart.vkservice.infrastructure.adapter.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.output.VkApiPort;
import ru.mart.vkservice.infrastructure.config.VkApiConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class VkApiAdapter implements VkApiPort {

    private final ProducerTemplate producerTemplate;
    private final VkApiConfig vkApiConfig;

    @Override
    public VkUser getUserInfo(Long userId, String serviceToken) {
        String url = buildUsersGetUrl(userId, serviceToken);
        // Implementation will be added in next commit with Camel
        return new VkUser(userId, "Test", "User", "Middle", false);
    }

    @Override
    public boolean isGroupMember(Long userId, Long groupId, String serviceToken) {
        String url = buildGroupsIsMemberUrl(userId, groupId, serviceToken);
        // Implementation will be added in next commit with Camel
        return false;
    }

    private String buildUsersGetUrl(Long userId, String serviceToken) {
        return String.format("%s/users.get?user_ids=%d&fields=first_name,last_name,middle_name&access_token=%s&v=%s",
                vkApiConfig.getBaseUrl(), userId, serviceToken, vkApiConfig.getVersion());
    }

    private String buildGroupsIsMemberUrl(Long userId, Long groupId, String serviceToken) {
        return String.format("%s/groups.isMember?user_id=%d&group_id=%d&access_token=%s&v=%s",
                vkApiConfig.getBaseUrl(), userId, groupId, serviceToken, vkApiConfig.getVersion());
    }
}
