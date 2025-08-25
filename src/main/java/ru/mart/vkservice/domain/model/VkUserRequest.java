package ru.mart.vkservice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class VkUserRequest {
    @NotNull(message = "user_id is required")
    @Positive(message = "user_id must be positive")
    @JsonProperty("user_id")
    private Long userId;

    @NotNull(message = "group_id is required")
    @Positive(message = "group_id must be positive")
    @JsonProperty("group_id")
    private Long groupId;
}
