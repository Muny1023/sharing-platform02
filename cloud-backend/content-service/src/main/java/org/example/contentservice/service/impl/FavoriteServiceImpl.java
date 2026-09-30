package org.example.contentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.cloudcommon.AccountSummary;
import org.example.contentservice.client.AccountClient;
import org.example.contentservice.entity.Favorite;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.FavoriteToggleRequest;
import org.example.contentservice.entity.response.FavoriteItemResponse;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.mapper.FavoriteMapper;
import org.example.contentservice.mapper.PostMapper;
import org.example.contentservice.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private AccountClient accountClient;

    @Override
    @Transactional
    public ToggleResult toggle(Integer userId, FavoriteToggleRequest request) {
        Integer postId = request.getPostId();
        Favorite existing = getOne(Wrappers.<Favorite>query().eq("user_id", userId).eq("post_id", postId));
        Post post = postMapper.selectById(postId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) {
            if (existing != null && removeById(existing.getId())) return new ToggleResult(false, null);
            return new ToggleResult(false, "帖子不存在");
        }
        if (existing != null) {
            if (!removeById(existing.getId())) return new ToggleResult(true, "内部错误，请联系管理员");
            postMapper.update(null, Wrappers.<Post>update().eq("id", postId)
                    .setSql("favorite_count = GREATEST(favorite_count - 1, 0)"));
            return new ToggleResult(false, null);
        }
        if (!save(new Favorite(null, userId, postId, new Date()))) return new ToggleResult(false, "内部错误，请联系管理员");
        postMapper.update(null, Wrappers.<Post>update().eq("id", postId)
                .setSql("favorite_count = favorite_count + 1"));
        return new ToggleResult(true, null);
    }

    @Override
    public boolean isFavorited(Integer userId, Integer postId) {
        return exists(Wrappers.<Favorite>query().eq("user_id", userId).eq("post_id", postId));
    }

    @Override
    public PageResponse<FavoriteItemResponse> listMine(Integer userId, int page, int size, String sort) {
        Page<Favorite> result = new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 50));
        if ("hot".equals(sort)) {
            favoriteMapper.selectByHot(result, userId);
        } else {
            var query = Wrappers.<Favorite>query().eq("user_id", userId);
            if ("oldest".equals(sort)) query.orderByAsc("create_time");
            else query.orderByDesc("create_time");
            page(result, query);
        }

        List<Integer> postIds = result.getRecords().stream().map(Favorite::getPostId).toList();
        Map<Integer, Post> posts = postIds.isEmpty() ? Map.of() : postMapper.selectBatchIds(postIds).stream()
                .collect(Collectors.toMap(Post::getId, post -> post));
        Map<Integer, String> names = loadNicknames(posts);
        List<FavoriteItemResponse> items = result.getRecords().stream().map(favorite -> {
            Post post = posts.get(favorite.getPostId());
            boolean deleted = Boolean.TRUE.equals(favorite.getPostDeleted()) || post == null
                    || Boolean.TRUE.equals(post.getDeleted());
            return new FavoriteItemResponse(favorite.getId(), favorite.getPostId(),
                    post == null ? "未知用户" : names.getOrDefault(post.getAuthorId(), "未知用户"),
                    deleted ? "" : post.getTitle(), deleted ? "" : post.getContent(),
                    deleted ? "" : post.getResourceUrl(), post == null ? 0 : post.getLikeCount(),
                    post == null ? 0 : post.getCommentCount(), post == null ? null : post.getCreateTime(),
                    favorite.getCreateTime(), deleted);
        }).toList();
        return new PageResponse<>(result.getCurrent(), result.getSize(), result.getTotal(), items);
    }

    private Map<Integer, String> loadNicknames(Map<Integer, Post> posts) {
        if (posts.isEmpty()) return Map.of();
        try {
            return accountClient.findByIds(posts.values().stream().map(Post::getAuthorId).distinct().toList())
                    .stream().collect(Collectors.toMap(AccountSummary::id, AccountSummary::nickname,
                            (first, ignored) -> first));
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }
}
