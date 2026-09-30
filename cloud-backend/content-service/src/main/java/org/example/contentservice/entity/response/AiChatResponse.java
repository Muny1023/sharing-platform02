package org.example.contentservice.entity.response;

import java.util.List;

public record AiChatResponse(String conversationId, String reply, String summary,
                             List<AiPostCardResponse> posts) {
}
