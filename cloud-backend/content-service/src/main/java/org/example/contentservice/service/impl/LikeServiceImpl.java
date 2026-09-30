package org.example.contentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.contentservice.entity.Comment;
import org.example.contentservice.entity.Like;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.LikeToggleRequest;
import org.example.contentservice.mapper.CommentMapper;
import org.example.contentservice.mapper.LikeMapper;
import org.example.contentservice.mapper.PostMapper;
import org.example.contentservice.service.LikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {
    @Resource
    private PostMapper postMapper;

    @Resource
    private CommentMapper commentMapper;

    @Override
    @Transactional
    public LikeToggleResult toggleLike(Integer userId, LikeToggleRequest request) {
        boolean postTarget = "post".equals(request.getTargetType());
        boolean commentTarget = "comment".equals(request.getTargetType());
        if (!postTarget && !commentTarget) return LikeToggleResult.failure("目标类型只能是 post 或 comment");

        if (postTarget) {
            Post post = postMapper.selectById(request.getTargetId());
            if (post == null || Boolean.TRUE.equals(post.getDeleted())) return LikeToggleResult.failure("帖子不存在或已被删除");
        } else if (commentMapper.selectById(request.getTargetId()) == null) {
            return LikeToggleResult.failure("评论不存在或已被删除");
        }

        String targetColumn = postTarget ? "post_id" : "comment_id";
        Like existing = getOne(Wrappers.<Like>query().eq("user_id", userId)
                .eq(targetColumn, request.getTargetId()));
        int delta = existing == null ? 1 : -1;
        if (existing == null) {
            boolean saved = save(postTarget
                    ? new Like(null, userId, request.getTargetId(), null, new Date())
                    : new Like(null, userId, null, request.getTargetId(), new Date()));
            if (!saved) return LikeToggleResult.failure("内部错误，请联系管理员");
        } else if (!removeById(existing.getId())) {
            return LikeToggleResult.failure("内部错误，请联系管理员");
        }

        boolean updated = postTarget
                ? postMapper.update(null, Wrappers.<Post>update().eq("id", request.getTargetId())
                .setSql("like_count = GREATEST(like_count + " + delta + ", 0)")) > 0
                : commentMapper.update(null, Wrappers.<Comment>update().eq("id", request.getTargetId())
                .setSql("like_count = GREATEST(like_count + " + delta + ", 0)")) > 0;
        if (!updated) return LikeToggleResult.failure("内部错误，请联系管理员");
        return LikeToggleResult.success(existing == null, 0);
    }

    @Override
    public Set<Integer> findLikedTargetIds(Integer userId, String targetType, List<Integer> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) return Set.of();
        String column = "post".equals(targetType) ? "post_id" : "comment_id";
        return list(Wrappers.<Like>query().eq("user_id", userId).in(column, targetIds)).stream()
                .map(like -> "post".equals(targetType) ? like.getPostId() : like.getCommentId())
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }
}
