package ru.mart.vkservice.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VkUserResponseDto {
    private String lastName;
    private String firstName;
    private String middleName;
    private Boolean member;
}
