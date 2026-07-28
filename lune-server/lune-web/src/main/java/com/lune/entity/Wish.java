package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 许愿池 —— 用户提出的程序需求/心愿
 */
@Data
@TableName("wish")
public class Wish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 心愿标题 */
    private String title;
    /** 需求详情 */
    private String content;
    /** 点赞数（冗余，便于按热度排序） */
    private Long likeCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
    /** 当前用户是否已点赞 */
    @TableField(exist = false)
    private Boolean liked;
    /** 评论数 */
    @TableField(exist = false)
    private Long commentCount;
}
