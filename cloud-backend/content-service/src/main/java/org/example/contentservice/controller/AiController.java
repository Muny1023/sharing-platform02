package org.example.contentservice.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.cloudcommon.RestBean;
import org.example.contentservice.client.NotificationClient;
import org.example.contentservice.entity.Comment;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.AiChatRequest;
import org.example.contentservice.entity.request.AiPostContentRequest;
import org.example.contentservice.entity.request.AiPostFeedbackRequest;
import org.example.contentservice.entity.request.AiSearchRequest;
import org.example.contentservice.entity.response.AiChatResponse;
import org.example.contentservice.entity.response.AiCommentItemResponse;
import org.example.contentservice.entity.response.AiPostContentResponse;
import org.example.contentservice.entity.response.AiPostFeedbackResponse;
import org.example.contentservice.service.AiService;
import org.example.contentservice.service.CommentService;
import org.example.contentservice.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
public class AiController {
    @Resource
    private AiService aiService;

    @Resource
    private PostService postService;

    @Resource
    private CommentService commentService;

    @Resource
    private NotificationClient notificationClient;

    @Value("${ai.service-token:change-this-local-token}")
    String serviceToken;

    @PostMapping("/api/ai/chat")
    public ResponseEntity<RestBean<AiChatResponse>> chat(@RequestBody @Valid AiChatRequest request,
                                                         jakarta.servlet.http.HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("id");
        try {
            return ResponseEntity.ok(RestBean.success(aiService.chat(userId, request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(RestBean.failure(400, e.getMessage()));
        } catch (AiService.AiServiceException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(RestBean.failure(503, e.getMessage()));
        }
    }

    @PostMapping("/internal/ai/post-content")
    public ResponseEntity<RestBean<AiPostContentResponse>> postContent(
            @RequestHeader(value = "X-AI-Service-Token", required = false) String token,
            @RequestBody @Valid AiPostContentRequest request) {
        ResponseEntity<RestBean<AiPostContentResponse>> unauthorized = unauthorized(token);
        if (unauthorized != null) return unauthorized;
        Post post = postService.getById(request.postId());
        if (post == null) return ResponseEntity.status(404).body(RestBean.failure(404, "帖子不存在"));
        if (Boolean.TRUE.equals(post.getDeleted())) {
            return ResponseEntity.status(410).body(RestBean.failure(410, "该帖子已被作者删除"));
        }
        return ResponseEntity.ok(RestBean.success(new AiPostContentResponse(post.getId(), post.getTitle(), post.getContent())));
    }

    @PostMapping("/internal/ai/post-feedback")
    public ResponseEntity<RestBean<AiPostFeedbackResponse>> postFeedback(
            @RequestHeader(value = "X-AI-Service-Token", required = false) String token,
            @RequestBody @Valid AiPostFeedbackRequest request) {
        ResponseEntity<RestBean<AiPostFeedbackResponse>> unauthorized = unauthorized(token);
        if (unauthorized != null) return unauthorized;
        Post post = postService.getById(request.postId());
        if (post == null) return ResponseEntity.status(404).body(RestBean.failure(404, "帖子不存在"));
        if (Boolean.TRUE.equals(post.getDeleted())) {
            return ResponseEntity.status(410).body(RestBean.failure(410, "该帖子已被作者删除"));
        }
        if (!request.userId().equals(post.getAuthorId())) {
            return ResponseEntity.status(403).body(RestBean.failure(403, "只能分析自己发布的帖子"));
        }
        List<Comment> comments = commentService.list(Wrappers.<Comment>query()
                .eq("post_id", post.getId()).orderByAsc("create_time"));
        if (request.unreadOnly()) {
            List<Integer> unreadIds = notificationClient.unreadCommentIds(
                    Map.of("recipientId", request.userId(), "postId", request.postId()));
            var unread = new HashSet<>(unreadIds);
            comments = comments.stream().filter(comment -> unread.contains(comment.getId())).toList();
        }
        boolean partial = comments.size() > 100;
        List<AiCommentItemResponse> selected = comments.stream().skip(Math.max(0, comments.size() - 100L))
                .map(comment -> new AiCommentItemResponse(comment.getId(), comment.getParentId(),
                        comment.getContent(), comment.getCreateTime())).toList();
        return ResponseEntity.ok(RestBean.success(new AiPostFeedbackResponse(
                post.getId(), post.getContent(), post.getUpdateTime(), selected, partial)));
    }

    @PostMapping("/internal/ai/search")
    public ResponseEntity<RestBean<?>> search(
            @RequestHeader(value = "X-AI-Service-Token", required = false) String token,
            @RequestBody @Valid AiSearchRequest request) {
        ResponseEntity<RestBean<AiChatResponse>> unauthorized = unauthorized(token);
        if (unauthorized != null) return ResponseEntity.status(401).body(unauthorized.getBody());
        return ResponseEntity.ok(RestBean.success(postService.searchPosts(
                request.keyword() == null ? "" : request.keyword().trim(), request.page(), request.size())));
    }

    private <T> ResponseEntity<RestBean<T>> unauthorized(String token) {
        if (token == null || !MessageDigest.isEqual(serviceToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(401).body(RestBean.failure(401, "无效的服务凭证"));
        }
        return null;
    }
}
