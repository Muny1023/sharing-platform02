package org.example.contentservice.controller;

import org.example.cloudcommon.RestBean;
import org.example.contentservice.entity.response.PostDetailResponse;
import org.example.contentservice.mapper.PostTombstoneMapper;
import org.example.contentservice.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {
    @Mock
    private PostService postService;

    @Mock
    private PostTombstoneMapper tombstoneMapper;

    @InjectMocks
    private PostController controller;

    @Test
    void detailReturnsPostWithoutSensitiveData() {
        when(postService.getPostDetail(1)).thenReturn(new PostDetailResponse(1, 2, "用户2", "标题",
                "正文", "https://example.com/a", 0, 0, new Date(), new Date(), false, 0));

        RestBean<PostDetailResponse> response = controller.detail(1);

        assertEquals(200, response.code());
        assertEquals("标题", response.data().title());
        assertEquals(2, response.data().authorId());
    }

    @Test
    void detailReturns410ForTombstone() {
        when(postService.getPostDetail(1)).thenReturn(null);
        when(tombstoneMapper.selectById(1)).thenReturn(new org.example.contentservice.entity.PostTombstone());

        RestBean<PostDetailResponse> response = controller.detail(1);

        assertEquals(410, response.code());
        assertEquals("该帖子已被作者删除", response.message());
    }
}
