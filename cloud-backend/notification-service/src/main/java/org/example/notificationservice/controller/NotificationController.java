package org.example.notificationservice.controller;

import jakarta.annotation.Resource;
import org.example.cloudcommon.RestBean;
import org.example.notificationservice.entity.response.NotificationPageResponse;
import org.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Resource
    private NotificationService notificationService;

    @GetMapping("/unread-count")
    public RestBean<Long> unreadCount(@RequestAttribute("id") Integer recipientId) {
        return RestBean.success(notificationService.unreadCount(recipientId));
    }

    @GetMapping
    public RestBean<NotificationPageResponse> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String filter,
            @RequestAttribute("id") Integer recipientId) {
        return RestBean.success(notificationService.page(recipientId, page, size, filter));
    }

    @PutMapping("/{notificationId}/read")
    public RestBean<Void> read(@PathVariable Integer notificationId,
                               @RequestAttribute("id") Integer recipientId) {
        return message(() -> notificationService.markRead(recipientId, notificationId));
    }

    @PutMapping("/read-all")
    public RestBean<Long> readAll(@RequestAttribute("id") Integer recipientId) {
        return RestBean.success(notificationService.markAllRead(recipientId));
    }

    @DeleteMapping("/{notificationId}")
    public RestBean<Void> delete(@PathVariable Integer notificationId,
                                 @RequestAttribute("id") Integer recipientId) {
        return message(() -> notificationService.deleteOne(recipientId, notificationId));
    }

    @DeleteMapping("/read")
    public RestBean<Long> deleteRead(@RequestAttribute("id") Integer recipientId) {
        return RestBean.success(notificationService.deleteRead(recipientId));
    }

    private RestBean<Void> message(Supplier<String> operation) {
        String message = operation.get();
        return message == null ? RestBean.success() : RestBean.failure(404, message);
    }
}
