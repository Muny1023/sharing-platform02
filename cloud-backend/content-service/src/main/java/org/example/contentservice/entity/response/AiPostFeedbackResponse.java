package org.example.contentservice.entity.response;

import java.util.Date;
import java.util.List;

public record AiPostFeedbackResponse(Integer postId, String content, Date updateTime,
                                     List<AiCommentItemResponse> comments, boolean partial) {
}
