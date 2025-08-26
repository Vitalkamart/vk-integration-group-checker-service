package ru.mart.vkservice.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mart.vkservice.infrastructure.controller.dto.ClearCacheRequest;

@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management", description = "Управление кэшированием")
public class CacheController {

    private final ProducerTemplate producerTemplate;

    @Operation(summary = "Очистить кэш для конкретного запроса")
    @PostMapping("/clear")
    public ResponseEntity<Void> clearCache(@RequestBody ClearCacheRequest request) {
        String cacheKey = "vk_user_" + request.getUserId() + "_group_" + request.getGroupId();
        producerTemplate.sendBody("direct:clearCacheByKey", cacheKey);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Очистить весь кэш")
    @PostMapping("/clear-all")
    public ResponseEntity<Void> clearAllCache() {
        producerTemplate.sendBody("direct:clearAllCache", null);
        return ResponseEntity.ok().build();
    }
}
