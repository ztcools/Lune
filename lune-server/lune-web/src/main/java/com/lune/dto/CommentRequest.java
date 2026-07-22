package com.lune.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    private Long articleId;
    @NotBlank(message = "评论内容不能为空")
    private String content;
    private Long parentId;
    private Long replyTo;
}
