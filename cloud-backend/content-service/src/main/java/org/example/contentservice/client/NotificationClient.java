package org.example.contentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/internal/notifications/unread-comment-ids")
    List<Integer> unreadCommentIds(@RequestBody Map<String, Integer> request);
}
