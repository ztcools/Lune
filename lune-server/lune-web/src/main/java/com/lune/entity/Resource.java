package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String filename;
    private String path;
    private Long size;
    private String mimeType;
    private String type;
    private String storeType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
