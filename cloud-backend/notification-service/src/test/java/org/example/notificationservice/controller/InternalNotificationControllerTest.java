package org.example.notificationservice.controller;

import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalNotificationControllerTest {
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private InternalNotificationController controller;

    @Test
    void unreadCommentIdsUsesBatchRequest() {
        when(notificationService.listUnreadCommentIds(2, 10)).thenReturn(List.of(3, 4));

        assertEquals(List.of(3, 4), controller.unreadCommentIds(Map.of("recipientId", 2, "postId", 10)));
    }

    @Test
    void incompleteRequestReturnsEmptyList() {
        assertEquals(List.of(), controller.unreadCommentIds(Map.of("recipientId", 2)));
    }
}
