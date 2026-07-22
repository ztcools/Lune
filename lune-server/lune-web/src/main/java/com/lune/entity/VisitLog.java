package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visit_log")
public class VisitLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ip;
    private String userAgent;
    private String path;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
