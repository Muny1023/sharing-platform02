package org.example.contentservice.entity.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AiPostContentRequest(@NotNull @Positive Integer postId,
                                    @NotNull @Positive Integer userId) {
}
