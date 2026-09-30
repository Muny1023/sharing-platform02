package org.example.contentservice.entity.response;

import java.util.Date;

public record FavoriteItemResponse(Integer favoriteId, Integer postId, String authorNickname,
                                   String title, String content, String resourceUrl,
                                   Integer likeCount, Integer commentCount, Date postCreateTime,
                                   Date favoriteCreateTime, boolean deleted) {
}
