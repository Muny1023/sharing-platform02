package org.example.contentservice.entity.response;

import java.util.Date;

public record PostListResponse(Integer id, Integer authorId, String authorNickname, String title,
                               String content, String resourceUrl, Integer likeCount,
                               Integer commentCount, Date createTime, boolean deleted,
                               Integer favoriteCount) {
}
