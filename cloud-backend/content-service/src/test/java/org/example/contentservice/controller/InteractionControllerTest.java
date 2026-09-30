package org.example.contentservice.controller;

import org.example.cloudcommon.RestBean;
import org.example.contentservice.entity.request.CommentCreateRequest;
import org.example.contentservice.entity.request.FavoriteToggleRequest;
import org.example.contentservice.entity.request.LikeToggleRequest;
import org.example.contentservice.entity.response.CommentResponse;
import org.example.contentservice.entity.response.FavoriteItemResponse;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.service.CommentService;
import org.example.contentservice.service.FavoriteService;
import org.example.contentservice.service.LikeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractionControllerTest {
    @Mock
    private CommentService commentService;
    @Mock
    private LikeService likeService;
    @Mock
    private FavoriteService favoriteService;
    @InjectMocks
    private CommentController commentController;
    @InjectMocks
    private LikeController likeController;
    @InjectMocks
    private FavoriteController favoriteController;

    @Test
    void commentCreateKeepsRestBeanErrorContract() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setPostId(10);
        request.setContent("回复");
        when(commentService.createComment(2, request)).thenReturn("帖子不存在或已被删除");

        RestBean<Void> response = commentController.create(request, 2);

        assertEquals(400, response.code());
        assertEquals("帖子不存在或已被删除", response.message());
    }

    @Test
    void commentListReturnsNestedResponse() {
        CommentResponse comment = new CommentResponse(1, 10, 2, "用户2", null,
                "正文", 0, new Date(), List.of());
        when(commentService.listCommentsByPost(10)).thenReturn(List.of(comment));

        RestBean<List<CommentResponse>> response = commentController.list(10);

        assertEquals(200, response.code());
        assertEquals(1, response.data().get(0).id());
        assertTrue(response.data().get(0).replies().isEmpty());
    }

    @Test
    void likeStatusRejectsUnknownTargetType() {
        RestBean<Set<Integer>> response = likeController.status("article", List.of(1), 2);

        assertEquals(400, response.code());
        assertEquals("目标类型只能是 post 或 comment", response.message());
    }

    @Test
    void likeToggleReturnsLikedFlag() {
        LikeToggleRequest request = new LikeToggleRequest();
        request.setTargetType("post");
        request.setTargetId(10);
        when(likeService.toggleLike(2, request)).thenReturn(LikeService.LikeToggleResult.success(true, 1));

        RestBean<Map<String, Object>> response = likeController.toggle(request, 2);

        assertEquals(200, response.code());
        assertEquals(Boolean.TRUE, response.data().get("liked"));
        verify(likeService).toggleLike(2, request);
    }

    @Test
    void favoriteListReturnsPageEnvelope() {
        PageResponse<FavoriteItemResponse> page = new PageResponse<>(1, 10, 1,
                List.of(new FavoriteItemResponse(5, 10, "用户2", "标题", "正文", "url",
                        1, 2, new Date(), new Date(), false)));
        when(favoriteService.listMine(2, 1, 10, "latest")).thenReturn(page);

        RestBean<PageResponse<FavoriteItemResponse>> response = favoriteController.list(1, 10, "latest", 2);

        assertEquals(200, response.code());
        assertEquals(1, response.data().total());
        assertEquals(10, response.data().items().get(0).postId());
    }

    @Test
    void favoriteToggleReturnsFavoritedFlag() {
        FavoriteToggleRequest request = new FavoriteToggleRequest();
        request.setPostId(10);
        when(favoriteService.toggle(2, request)).thenReturn(new FavoriteService.ToggleResult(true, null));

        RestBean<Map<String, Object>> response = favoriteController.toggle(request, 2);

        assertEquals(200, response.code());
        assertEquals(Boolean.TRUE, response.data().get("favorited"));
    }
}
