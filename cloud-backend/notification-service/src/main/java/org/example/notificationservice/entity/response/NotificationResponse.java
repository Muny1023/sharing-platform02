package org.example.notificationservice.entity.response;

import java.util.Date;

public record NotificationResponse(Integer id, Integer actorId, String actorNickname, String type,
                                   Integer postId, Integer commentId, String content, Date createdAt,
                                   boolean read, boolean postDeleted, boolean commentDeleted) {
}
