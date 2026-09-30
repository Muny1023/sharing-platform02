package org.example.cloudcommon;

/**
 * Message exchanged between content and notification services.
 */
public record NotificationEvent(String eventId, Integer actorId, Integer recipientId,
                                String type, Integer postId, Integer commentId,
                                String content) {
}
