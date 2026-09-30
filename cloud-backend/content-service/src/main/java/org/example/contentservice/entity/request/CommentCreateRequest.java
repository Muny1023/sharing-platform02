package org.example.contentservice.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CommentCreateRequest {
    @NotNull(message = "帖子ID不能为空")
    private Integer postId;

    private Integer parentId;

    @NotBlank(message = "评论内容不能为空")
    @Length(max = 1000, message = "评论不能超过1000字")
    private String content;
}
