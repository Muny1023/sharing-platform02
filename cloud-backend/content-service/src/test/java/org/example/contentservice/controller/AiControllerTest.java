package org.example.contentservice.controller;

import org.example.cloudcommon.RestBean;
import org.example.contentservice.client.NotificationClient;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.AiPostContentRequest;
import org.example.contentservice.entity.request.AiPostFeedbackRequest;
import org.example.contentservice.entity.request.AiSearchRequest;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.entity.response.PostListResponse;
import org.example.contentservice.service.AiService;
import org.example.contentservice.service.CommentService;
import org.example.contentservice.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {
    @Mock
    private AiService aiService;
    @Mock
    private PostService postService;
    @Mock
    private CommentService commentService;
    @Mock
    private NotificationClient notificationClient;
    @InjectMocks
    private AiController controller;

    @BeforeEach
    void configureToken() {
        controller.serviceToken = "secret";
    }

    @Test
    void internalPostContentRequiresAiToken() {
        ResponseEntity<RestBean<org.example.contentservice.entity.response.AiPostContentResponse>> response =
                controller.postContent("wrong", new AiPostContentRequest(1, 2));

        assertEquals(401, response.getStatusCode().value());
        assertEquals("无效的服务凭证", response.getBody().message());
    }

    @Test
    void internalPostContentReturnsVisiblePost() {
        Date now = new Date();
        when(postService.getById(1)).thenReturn(new Post(1, 2, "标题", "正文", "url", 0,
                0, now, now, false, 0));

        var response = controller.postContent("secret", new AiPostContentRequest(1, 2));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("正文", response.getBody().data().content());
    }

    @Test
    void feedbackRejectsPostsOwnedByAnotherUser() {
        Date now = new Date();
        when(postService.getById(1)).thenReturn(new Post(1, 7, "标题", "正文", "url", 0,
                0, now, now, false, 0));

        var response = controller.postFeedback("secret", new AiPostFeedbackRequest(1, 2, false));

        assertEquals(403, response.getStatusCode().value());
        assertEquals("只能分析自己发布的帖子", response.getBody().message());
    }

    @Test
    void searchPreservesRestBeanEnvelope() {
        when(postService.searchPosts("git", 1, 10)).thenReturn(new PageResponse<>(1, 10, 0,
                List.<PostListResponse>of()));

        var response = controller.search("secret", new AiSearchRequest("git", 1, 10));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().code());
    }
}
