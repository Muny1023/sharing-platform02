package org.example.contentservice.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.PostTombstone;
import org.example.contentservice.entity.request.PostCreateRequest;
import org.example.contentservice.entity.request.PostUpdateRequest;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.entity.response.PostDetailResponse;
import org.example.contentservice.entity.response.PostListResponse;
import org.example.contentservice.mapper.PostMapper;
import org.example.contentservice.mapper.PostTombstoneMapper;
import org.example.contentservice.mapper.FavoriteMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    private static final String POST_CREATE_INTERVAL = "post:create:interval:";
    private static final String POST_CREATE_DAILY = "post:create:daily:";
    private static final int DAILY_LIMIT = 50;

    @Resource
    private StringRedisTemplate redis;

    @Resource
    private PostTombstoneMapper tombstoneMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Override
    public String createPost(Integer authorId, PostCreateRequest request) {
        Boolean accepted = redis.opsForValue().setIfAbsent(
                POST_CREATE_INTERVAL + authorId, "", 30, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(accepted)) return "30秒内不能重复发帖，请稍后再试";
        Long count = redis.opsForValue().increment(POST_CREATE_DAILY + authorId);
        if (count != null && count == 1) redis.expire(POST_CREATE_DAILY + authorId, 1, TimeUnit.DAYS);
        if (count != null && count > DAILY_LIMIT) return "今日发帖已达上限（每天最多发布50帖）";
        Post post = new Post(null, authorId, request.getTitle(), request.getContent(), request.getResourceUrl(),
                0, 0, new Date(), new Date(), false, 0);
        return save(post) ? null : "内部错误，请联系管理员";
    }

    @Override
    public String updatePost(Integer authorId, Integer postId, PostUpdateRequest request) {
        Post post = getById(postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) return "帖子不存在或已被删除";
        if (!authorId.equals(post.getAuthorId())) return "只能编辑自己的帖子";
        var update = update().eq("id", postId).eq("author_id", authorId)
                .set("title", request.getTitle()).set("content", request.getContent())
                .set("resource_url", request.getResourceUrl());
        if (request.getExpectedUpdateTime() != null) update.eq("update_time", new Date(request.getExpectedUpdateTime()));
        return update.update() ? null : "帖子内容已被修改，请重新生成草稿";
    }

    @Override
    @Transactional
    public String deletePost(Integer authorId, Integer postId) {
        Post post = getById(postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) return "帖子不存在或已被删除";
        if (!authorId.equals(post.getAuthorId())) return "只能删除自己的帖子";
        Date now = new Date();
        tombstoneMapper.insert(new PostTombstone(postId, now,
                new Date(now.getTime() + TimeUnit.DAYS.toMillis(1))));
        favoriteMapper.markPostDeleted(postId);
        return removeById(postId) ? null : "内部错误，请联系管理员";
    }

    @Override
    public PageResponse<PostListResponse> listPosts(int page, int size, String sort) {
        var query = Wrappers.<Post>query().eq("deleted", false);
        if ("oldest".equals(sort)) query.orderByAsc("create_time");
        else query.orderByDesc("create_time");
        return toPage(page(new Page<>(page, size), query));
    }

    @Override
    public PageResponse<PostListResponse> listMyPosts(Integer authorId, int page, int size, String sort) {
        var query = Wrappers.<Post>query().eq("author_id", authorId).eq("deleted", false);
        if ("oldest".equals(sort)) query.orderByAsc("create_time");
        else query.orderByDesc("create_time");
        return toPage(page(new Page<>(page, size), query));
    }

    @Override
    public PageResponse<PostListResponse> searchPosts(String keyword, int page, int size) {
        var query = Wrappers.<Post>query().eq("deleted", false);
        if (keyword != null && !keyword.isBlank()) {
            query.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
        }
        return toPage(page(new Page<>(page, size), query.orderByDesc("create_time")));
    }

    @Override
    public PostDetailResponse getPostDetail(Integer id) {
        Post post = getById(id);
        if (post == null) return null;
        return new PostDetailResponse(post.getId(), post.getAuthorId(), nickname(post.getAuthorId()),
                post.getTitle(), post.getContent(), post.getResourceUrl(), post.getLikeCount(),
                post.getCommentCount(), post.getCreateTime(), post.getUpdateTime(),
                Boolean.TRUE.equals(post.getDeleted()), post.getFavoriteCount());
    }

    private PageResponse<PostListResponse> toPage(Page<Post> page) {
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(post -> new PostListResponse(post.getId(), post.getAuthorId(),
                        nickname(post.getAuthorId()), post.getTitle(), post.getContent(), post.getResourceUrl(),
                        post.getLikeCount(), post.getCommentCount(), post.getCreateTime(),
                        Boolean.TRUE.equals(post.getDeleted()), post.getFavoriteCount())).toList());
    }

    private String nickname(Integer authorId) {
        return authorId == null ? "未知用户" : "用户" + authorId;
    }
}
