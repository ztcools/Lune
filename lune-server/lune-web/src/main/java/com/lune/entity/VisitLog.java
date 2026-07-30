package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("visit_log")
public class VisitLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ip;
    private String country;
    private String province;
    private String city;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String userAgent;
    private String path;
    private String method;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
