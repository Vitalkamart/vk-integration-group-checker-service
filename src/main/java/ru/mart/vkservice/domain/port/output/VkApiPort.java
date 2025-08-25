package ru.mart.vkservice.domain.port.output;

import ru.mart.vkservice.domain.model.VkUser;

public interface VkApiPort {
    VkUser getUserInfo(Long userId, String serviceToken);

    boolean isGroupMember(Long userId, Long groupId, String serviceToken);
}
