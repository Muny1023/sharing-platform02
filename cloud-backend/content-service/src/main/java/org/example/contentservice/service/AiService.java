package org.example.contentservice.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.AiChatRequest;
import org.example.contentservice.entity.response.AiChatResponse;
import org.example.contentservice.entity.response.AiPostCardResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AiService {
    @Value("${ai.service-url:http://127.0.0.1:8000}")
    String serviceUrl;

    @Value("${ai.service-token:change-this-local-token}")
    String serviceToken;

    @Resource
    private PostService postService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public AiChatResponse chat(Integer userId, AiChatRequest request) {
        String conversationId = normalizeConversationId(request.conversationId());
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("conversationId", conversationId);
            body.put("message", request.message().trim());
            if (request.targetPostId() != null) body.put("targetPostId", request.targetPostId());
            if (request.feedbackPostId() != null) body.put("feedbackPostId", request.feedbackPostId());
            if (request.draftPostId() != null) body.put("draftPostId", request.draftPostId());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serviceUrl + "/chat"))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("X-AI-Service-Token", serviceToken)
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("AI service returned status={}, body={}", response.statusCode(), response.body());
                throw new AiServiceException("AI 服务暂时不可用");
            }
            JSONObject result = JSON.parseObject(response.body());
            String reply = result.getString("reply");
            if (reply == null || reply.isBlank()) reply = "暂时无法完成检索";
            List<Integer> postIds = new ArrayList<>();
            JSONArray ids = result.getJSONArray("postIds");
            if (ids != null) ids.forEach(node -> {
                if (node instanceof Number number) postIds.add(number.intValue());
            });
            return new AiChatResponse(conversationId, reply, result.getString("summary"), buildCards(postIds));
        } catch (AiServiceException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI service request failed, url={}", serviceUrl, e);
            throw new AiServiceException("AI 服务暂时不可用", e);
        }
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return UUID.randomUUID().toString();
        try {
            return UUID.fromString(conversationId).toString();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("会话标识无效");
        }
    }

    private List<AiPostCardResponse> buildCards(List<Integer> postIds) {
        if (postIds.isEmpty()) return List.of();
        Map<Integer, Post> postsById = new LinkedHashMap<>();
        postService.listByIds(postIds).stream()
                .filter(post -> !Boolean.TRUE.equals(post.getDeleted()))
                .forEach(post -> postsById.put(post.getId(), post));
        return postIds.stream().distinct().map(postsById::get).filter(java.util.Objects::nonNull)
                .map(post -> new AiPostCardResponse(post.getId(), post.getTitle(), snippet(post.getContent()),
                        "/post/" + post.getId())).toList();
    }

    private String snippet(String content) {
        if (content == null) return "";
        String normalized = content.strip();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160) + "...";
    }

    public static class AiServiceException extends RuntimeException {
        public AiServiceException(String message) { super(message); }
        public AiServiceException(String message, Throwable cause) { super(message, cause); }
    }
}
