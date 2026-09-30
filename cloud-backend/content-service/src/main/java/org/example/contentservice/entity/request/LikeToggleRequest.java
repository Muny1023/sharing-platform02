package org.example.contentservice.entity.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeToggleRequest {
    @NotNull(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "目标ID不能为空")
    private Integer targetId;
}
