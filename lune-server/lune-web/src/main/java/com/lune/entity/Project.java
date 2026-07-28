package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目经历 —— 简历页项目卡片
 */
@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 项目名称 */
    private String name;
    /** 一句话简介 */
    private String summary;
    /** 详细描述 */
    private String description;
    /** 技术栈 JSON 数组 ["Vue3","Spring Boot"] */
    private String techStack;
    /** 项目作用/角色 */
    private String role;
    /** 项目地址 */
    private String projectUrl;
    /** 源码地址 */
    private String repoUrl;
    /** 封面图 */
    private String cover;
    /** 展示媒体 JSON [{type:image|video, url}] */
    private String media;
    /** 开发时间段描述，如 "2023.06 - 2023.12" */
    private String devPeriod;
    /** 排序权重 */
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
