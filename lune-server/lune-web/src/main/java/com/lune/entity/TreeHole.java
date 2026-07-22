package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tree_hole")
public class TreeHole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String color;
    private Long likeCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
