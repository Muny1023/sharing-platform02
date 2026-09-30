package org.example.contentservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.contentservice.entity.Comment;
import org.example.contentservice.entity.request.CommentCreateRequest;
import org.example.contentservice.entity.response.CommentResponse;

import java.util.List;

public interface CommentService extends IService<Comment> {
    String createComment(Integer authorId, CommentCreateRequest request);

    List<CommentResponse> listCommentsByPost(Integer postId);
}
