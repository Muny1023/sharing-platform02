package org.example.notificationservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.cloudcommon.NotificationEvent;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.entity.response.NotificationPageResponse;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    long unreadCount(Integer recipientId);

    NotificationPageResponse page(Integer recipientId, int page, int size, String filter);

    String markRead(Integer recipientId, Integer notificationId);

    long markAllRead(Integer recipientId);

    String deleteOne(Integer recipientId, Integer notificationId);

    long deleteRead(Integer recipientId);

    void consume(NotificationEvent event);

    long cleanupExpired();

    List<Integer> listUnreadCommentIds(Integer recipientId, Integer postId);
}
