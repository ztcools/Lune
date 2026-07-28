package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作经历 —— 简历页时间线节点
 */
@Data
@TableName("work_experience")
public class WorkExperience {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 公司名称 */
    private String company;
    /** 岗位 */
    private String position;
    /** 工作地点 */
    private String location;
    /** 开始时间 */
    private LocalDate startDate;
    /** 结束时间，NULL 表示至今 */
    private LocalDate endDate;
    /** 是否当前在职 */
    private Boolean isCurrent;
    /** 工作内容描述 */
    private String description;
    /** 核心职责 */
    private String responsibilities;
    /** 媒体附件 JSON [{type:image|video, url}] */
    private String media;
    /** 排序权重，越小越靠前 */
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
