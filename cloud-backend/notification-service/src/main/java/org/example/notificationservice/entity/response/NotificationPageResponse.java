package org.example.notificationservice.entity.response;

import java.util.List;

public record NotificationPageResponse(long page, long size, long total, long unreadCount,
                                       List<NotificationResponse> items) {
}
