package ru.mart.vkservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.input.VkUserService;
import ru.mart.vkservice.domain.port.output.VkApiPort;

@Service
@RequiredArgsConstructor
public class VkUserServiceImpl implements VkUserService {

    private final VkApiPort vkApiPort;

    @Override
    public VkUser getUserInfoWithMembership(Long userId, Long groupId, String serviceToken) {
        VkUser user = vkApiPort.getUserInfo(userId, serviceToken);
        boolean isMember = vkApiPort.isGroupMember(userId, groupId, serviceToken);

        user.setIsMember(isMember);
        return user;
    }
}
