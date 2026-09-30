package org.example.notificationservice.controller;

import org.example.cloudcommon.RestBean;
import org.example.notificationservice.entity.response.NotificationPageResponse;
import org.example.notificationservice.entity.response.NotificationResponse;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    @Test
    void pageReturnsCompatibleEnvelope() {
        NotificationResponse item = new NotificationResponse(1, 2, "用户2", "POST_LIKE",
                10, null, "有人赞了你的帖子", new Date(), false, false, false);
        when(notificationService.page(3, 1, 20, "all"))
                .thenReturn(new NotificationPageResponse(1, 20, 1, 1, List.of(item)));

        RestBean<NotificationPageResponse> response = controller.page(1, 20, "all", 3);

        assertEquals(200, response.code());
        assertEquals(1, response.data().unreadCount());
        assertEquals("POST_LIKE", response.data().items().get(0).type());
    }

    @Test
    void missingNotificationMapsToNotFound() {
        when(notificationService.markRead(3, 404)).thenReturn("通知不存在");

        RestBean<Void> response = controller.read(404, 3);

        assertEquals(404, response.code());
        assertEquals("通知不存在", response.message());
    }

    @Test
    void markAllReadReturnsChangedCount() {
        when(notificationService.markAllRead(3)).thenReturn(4L);

        RestBean<Long> response = controller.readAll(3);

        assertEquals(200, response.code());
        assertEquals(4L, response.data());
    }
}
