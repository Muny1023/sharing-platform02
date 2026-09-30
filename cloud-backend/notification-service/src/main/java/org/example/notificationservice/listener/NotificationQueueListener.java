package org.example.notificationservice.listener;

import jakarta.annotation.Resource;
import org.example.cloudcommon.NotificationEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationQueueListener {
    @Resource
    private NotificationService notificationService;

    @RabbitListener(queues = "notification")
    public void consume(NotificationEvent event) {
        notificationService.consume(event);
    }
}
