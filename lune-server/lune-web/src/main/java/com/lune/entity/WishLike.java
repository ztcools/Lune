package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 许愿点赞记录（防重复点赞）
 */
@Data
@TableName("wish_like")
public class WishLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wishId;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
