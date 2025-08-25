package ru.mart.vkservice.infrastructure.adapter.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.mart.vkservice.domain.model.VkUser;
import ru.mart.vkservice.domain.port.output.VkApiPort;
import ru.mart.vkservice.infrastructure.config.VkApiConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class VkApiAdapter implements VkApiPort {

    private final ProducerTemplate producerTemplate;
    private final VkApiConfig vkApiConfig;
    private final ObjectMapper objectMapper;

    @Override
    public VkUser getUserInfo(Long userId, String serviceToken) {
        try {
            String url = buildUsersGetUrl(userId, serviceToken);
            log.debug("Calling VK API: {}", url);

            String response = producerTemplate.requestBody("http:" + url, null, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("error")) {
                handleVkError(rootNode.get("error"), "Failed to get user info");
            }

            JsonNode userNode = rootNode.get("response").get(0);
            return mapToVkUser(userNode);

        } catch (Exception e) {
            log.error("Error calling VK users.get API: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "VK API error: " + e.getMessage());
        }
    }

    @Override
    public boolean isGroupMember(Long userId, Long groupId, String serviceToken) {
        try {
            String url = buildGroupsIsMemberUrl(userId, groupId, serviceToken);
            log.debug("Calling VK API: {}", url);

            String response = producerTemplate.requestBody("http:" + url, null, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("error")) {
                handleVkError(rootNode.get("error"), "Failed to check group membership");
            }

            return rootNode.get("response").asInt() == 1;

        } catch (Exception e) {
            log.error("Error calling VK groups.isMember API: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "VK API error: " + e.getMessage());
        }
    }

    private VkUser mapToVkUser(JsonNode userNode) {
        return new VkUser(
                userNode.get("id").asLong(),
                userNode.get("first_name").asText(),
                userNode.get("last_name").asText(),
                userNode.has("middle_name") ? userNode.get("middle_name").asText() : null,
                false
        );
    }

    private void handleVkError(JsonNode errorNode, String defaultMessage) {
        int errorCode = errorNode.get("error_code").asInt();
        String errorMsg = errorNode.get("error_msg").asText();

        log.error("VK API Error {}: {}", errorCode, errorMsg);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("VK API Error %d: %s", errorCode, errorMsg));
    }

    private String buildUsersGetUrl(Long userId, String serviceToken) {
        return String.format("//%s/users.get?user_ids=%d&fields=first_name,last_name,middle_name&access_token=%s&v=%s",
                vkApiConfig.getBaseUrl().replace("https://", ""),
                userId, serviceToken, vkApiConfig.getVersion());
    }

    private String buildGroupsIsMemberUrl(Long userId, Long groupId, String serviceToken) {
        return String.format("//%s/groups.isMember?user_id=%d&group_id=%d&access_token=%s&v=%s",
                vkApiConfig.getBaseUrl().replace("https://", ""),
                userId, groupId, serviceToken, vkApiConfig.getVersion());
    }
}
