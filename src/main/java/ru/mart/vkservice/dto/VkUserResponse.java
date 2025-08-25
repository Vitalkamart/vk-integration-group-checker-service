package ru.mart.vkservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VkUserResponse {
    private String lastName;
    private String firstName;
    private String middleName;
    private Boolean member;
}
