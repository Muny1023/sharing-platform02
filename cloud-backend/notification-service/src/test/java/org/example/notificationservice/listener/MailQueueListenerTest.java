package org.example.notificationservice.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailQueueListenerTest {
    @Mock
    private JavaMailSender sender;

    @InjectMocks
    private MailQueueListener listener;

    @Test
    void registerCodeCreatesRegistrationMail() {
        ReflectionTestUtils.setField(listener, "username", "noreply@example.com");

        listener.sendMailMessage(Map.of("type", "register", "email", "user@example.com", "code", 123456));

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        assertEquals("noreply@example.com", captor.getValue().getFrom());
        assertEquals("user@example.com", captor.getValue().getTo()[0]);
        assertEquals("欢迎注册我们的网站", captor.getValue().getSubject());
    }

    @Test
    void unknownMailTypeIsIgnored() {
        listener.sendMailMessage(Map.of("type", "other", "email", "user@example.com", "code", 123456));

        org.mockito.Mockito.verifyNoInteractions(sender);
    }
}
