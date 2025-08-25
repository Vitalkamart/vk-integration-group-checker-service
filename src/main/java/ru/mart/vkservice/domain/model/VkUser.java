package ru.mart.vkservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VkUser {
    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private Boolean isMember;
}
