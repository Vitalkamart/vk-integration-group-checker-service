package ru.mart.vkservice.infrastructure.controller.dto;

import lombok.Data;

@Data
public class ClearCacheRequest {
    private Long userId;
    private Long groupId;
}
