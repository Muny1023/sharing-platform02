package org.example.contentservice.entity.request;

import jakarta.validation.constraints.NotNull;

public record AiPostFeedbackRequest(@NotNull Integer postId,
                                     @NotNull Integer userId,
                                     boolean unreadOnly) {
}
