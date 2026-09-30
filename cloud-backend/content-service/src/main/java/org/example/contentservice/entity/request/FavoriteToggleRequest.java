package org.example.contentservice.entity.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteToggleRequest {
    @NotNull(message = "帖子ID不能为空")
    private Integer postId;
}
