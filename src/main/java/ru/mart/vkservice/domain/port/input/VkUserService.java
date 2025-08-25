package ru.mart.vkservice.domain.port.input;

import ru.mart.vkservice.domain.model.VkUser;

public interface VkUserService {
    VkUser getUserInfoWithMembership(Long userId, Long groupId, String serviceToken);
}
