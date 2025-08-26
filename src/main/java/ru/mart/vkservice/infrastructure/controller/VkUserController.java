package ru.mart.vkservice.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.infrastructure.adapter.output.CamelVkApiAdapter;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserRequestDto;
import ru.mart.vkservice.infrastructure.controller.dto.VkUserResponseDto;

@Slf4j
@RestController
@RequestMapping("/api/v1/vk-users")
@RequiredArgsConstructor
@Tag(name = "VK Users", description = "API для получения информации о пользователях VK")
public class VkUserController {

    private final CamelVkApiAdapter vkApiAdapter;

    @Operation(
            summary = "Получить информацию о пользователе VK",
            description = "Возвращает ФИО пользователя и информацию о членстве в группе",
            security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(schema = @Schema(implementation = VkUserResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "lastName": "Иванов",
                                      "firstName": "Иван",
                                      "middleName": "Иванович",
                                      "member": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера или VK API")
    })
    @PostMapping("/info")
    public ResponseEntity<VkUserResponseDto> getUserInfo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для запроса",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = VkUserRequestDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "user_id": 12345,
                                      "group_id": 67890
                                    }
                                    """)
                    )
            )
            @RequestHeader("vk_service_token")
            @Schema(description = "Сервисный токен VK приложения", example = "vk1234567890abcdef")
            String serviceToken,

            @Valid @RequestBody VkUserRequestDto request) {

        log.info("Received request for user ID: {}, group ID: {}", request.getUserId(), request.getGroupId());

        VkUser user = vkApiAdapter.getUserInfoWithMembership(
                request.getUserId(), request.getGroupId(), serviceToken);

        VkUserResponseDto response = new VkUserResponseDto(
                user.getLastName(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getIsMember()
        );

        log.debug("Response prepared for user: {} {}", user.getFirstName(), user.getLastName());
        return ResponseEntity.ok(response);
    }
}
