package com.lune.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String summary;
    private String cover;
    private Long categoryId;
    private Integer status;
    private List<Long> tagIds;
}
