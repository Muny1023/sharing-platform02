package org.example.contentservice.entity.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public record CommentResponse(Integer id, Integer postId, Integer authorId, String authorNickname,
                              Integer parentId, String content, Integer likeCount, Date createTime,
                              List<CommentResponse> replies) {
    public CommentResponse {
        replies = replies == null ? new ArrayList<>() : replies;
    }
}
