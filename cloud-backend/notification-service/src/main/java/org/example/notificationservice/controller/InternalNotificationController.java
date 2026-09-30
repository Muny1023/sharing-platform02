package org.example.notificationservice.controller;

import jakarta.annotation.Resource;
import org.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {
    @Resource
    private NotificationService notificationService;

    @PostMapping("/unread-comment-ids")
    public List<Integer> unreadCommentIds(@RequestBody Map<String, Integer> request) {
        if (request == null || request.get("recipientId") == null || request.get("postId") == null) return List.of();
        return notificationService.listUnreadCommentIds(request.get("recipientId"), request.get("postId"));
    }
}
