package org.example.contentservice.entity.response;

import java.util.Date;

public record PostDetailResponse(Integer id, Integer authorId, String authorNickname, String title,
                                 String content, String resourceUrl, Integer likeCount,
                                 Integer commentCount, Date createTime, Date updateTime,
                                 boolean deleted, Integer favoriteCount) {
}
