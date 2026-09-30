package org.example.contentservice.entity.response;

import java.util.Date;

public record AiCommentItemResponse(Integer id, Integer parentId, String content, Date createTime) {
}
