package org.example.contentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.cloudcommon.NotificationEvent;
import org.example.cloudcommon.AccountSummary;
import org.example.contentservice.client.AccountClient;
import org.example.contentservice.entity.Comment;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.CommentCreateRequest;
import org.example.contentservice.entity.response.CommentResponse;
import org.example.contentservice.mapper.CommentMapper;
import org.example.contentservice.service.CommentService;
import org.example.contentservice.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Resource
    private PostService postService;

    @Resource
    private AccountClient accountClient;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Override
    @Transactional
    public String createComment(Integer authorId, CommentCreateRequest request) {
        Post post = postService.getById(request.getPostId());
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) return "帖子不存在或已被删除";

        if (request.getParentId() != null) {
            Comment parent = getById(request.getParentId());
            if (parent == null) return "要回复的评论不存在或已被删除";
            if (parent.getParentId() != null) return "最多只能回复一级评论";
            if (!request.getPostId().equals(parent.getPostId())) return "回复目标与帖子不匹配";
        }

        Date now = new Date();
        Comment comment = new Comment(null, request.getPostId(), authorId, request.getParentId(),
                request.getContent(), 0, now, now);
        if (!save(comment)) return "内部错误，请联系管理员";
        boolean updated = postService.update().eq("id", request.getPostId())
                .setSql("comment_count = comment_count + 1").update();
        if (!updated) return "内部错误，请联系管理员";
        Integer recipientId = request.getParentId() == null ? post.getAuthorId()
                : getById(request.getParentId()).getAuthorId();
        if (recipientId != null && !recipientId.equals(authorId)) {
            amqpTemplate.convertAndSend("notification", new NotificationEvent(
                    UUID.randomUUID().toString(), authorId, recipientId,
                    request.getParentId() == null ? "POST_COMMENT" : "COMMENT_REPLY",
                    request.getPostId(), comment.getId(),
                    request.getParentId() == null ? "评论了你的帖子" : "回复了你的评论"));
        }
        return null;
    }

    @Override
    public List<CommentResponse> listCommentsByPost(Integer postId) {
        List<Comment> comments = list(Wrappers.<Comment>query().eq("post_id", postId).orderByAsc("create_time"));
        if (comments.isEmpty()) return List.of();
        Map<Integer, String> nicknames = loadNicknames(comments);
        Map<Integer, CommentResponse> topLevel = new LinkedHashMap<>();
        Map<Integer, List<CommentResponse>> replies = new LinkedHashMap<>();
        for (Comment comment : comments) {
            CommentResponse response = new CommentResponse(comment.getId(), comment.getPostId(),
                    comment.getAuthorId(), nicknames.getOrDefault(comment.getAuthorId(), "未知用户"),
                    comment.getParentId(), comment.getContent(), comment.getLikeCount(),
                    comment.getCreateTime(), new ArrayList<>());
            if (comment.getParentId() == null) topLevel.put(comment.getId(), response);
            else replies.computeIfAbsent(comment.getParentId(), ignored -> new ArrayList<>()).add(response);
        }
        topLevel.forEach((id, response) -> response.replies().addAll(replies.getOrDefault(id, List.of())));
        return new ArrayList<>(topLevel.values());
    }

    private Map<Integer, String> loadNicknames(List<Comment> comments) {
        List<Integer> ids = comments.stream().map(Comment::getAuthorId).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        try {
            return accountClient.findByIds(ids).stream().collect(Collectors.toMap(
                    AccountSummary::id, AccountSummary::nickname, (first, ignored) -> first));
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }
}
