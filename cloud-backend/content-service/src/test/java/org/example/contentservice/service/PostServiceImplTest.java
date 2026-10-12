package org.example.contentservice.service;

import org.example.cloudcommon.AccountSummary;
import org.example.contentservice.client.AccountClient;
import org.example.contentservice.entity.Post;
import org.example.contentservice.mapper.FavoriteMapper;
import org.example.contentservice.mapper.PostMapper;
import org.example.contentservice.mapper.PostTombstoneMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {
    @Mock
    private PostMapper postMapper;

    @Mock
    private PostTombstoneMapper tombstoneMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private AccountClient accountClient;

    @Mock
    private StringRedisTemplate redis;

    @InjectMocks
    private PostServiceImpl service;

    @Test
    void detailUsesAccountNicknameInsteadOfAuthorId() {
        Date now = new Date();
        Post post = new Post(1, 2, "标题", "正文", "https://example.com", 0, 0,
                now, now, false, 0);
        when(postMapper.selectById(1)).thenReturn(post);
        when(accountClient.findByIds(List.of(2)))
                .thenReturn(List.of(new AccountSummary(2, "user-two", "张三", null, "user")));

        var result = service.getPostDetail(1);

        assertEquals("张三", result.authorNickname());
    }
}
