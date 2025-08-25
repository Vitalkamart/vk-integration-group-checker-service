package ru.mart.vkservice.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.input.VkUserService;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserRequestDto;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserResponseDto;

@RestController
@RequestMapping("/api/v1/vk-users")
@RequiredArgsConstructor
public class VkUserController {

    private final VkUserService vkUserService;

    @PostMapping("/info")
    public ResponseEntity<VkUserResponseDto> getUserInfo(
            @RequestHeader("vk_service_token") String serviceToken,
            @Valid @RequestBody VkUserRequestDto request) {

        VkUser user = vkUserService.getUserInfoWithMembership(
                request.getUserId(), request.getGroupId(), serviceToken);

        VkUserResponseDto response = new VkUserResponseDto(
                user.getLastName(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getIsMember()
        );

        return ResponseEntity.ok(response);
    }
}
