package org.example.contentservice.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        String conversationId,
        Integer targetPostId,
        Integer feedbackPostId,
        Integer draftPostId,
        @NotBlank(message = "请输入想查找的内容")
        @Size(max = 500, message = "问题不能超过500个字符")
        String message) {
}
