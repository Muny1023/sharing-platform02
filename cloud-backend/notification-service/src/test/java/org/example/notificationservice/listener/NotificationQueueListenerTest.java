package org.example.notificationservice.listener;

import org.example.cloudcommon.NotificationEvent;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationQueueListenerTest {
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationQueueListener listener;

    @Test
    void forwardsEventToNotificationService() {
        NotificationEvent event = new NotificationEvent("event-1", 2, 3,
                "POST_LIKE", 10, null, "有人赞了你的帖子");

        listener.consume(event);

        verify(notificationService).consume(event);
    }
}
