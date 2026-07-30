package com.lune.entity;

/**
 * 带「作者信息」瞬时字段的实体。
 *
 * username/nickname/avatar 都是 {@code @TableField(exist = false)}，不落库，
 * 查询后由 {@link com.lune.service.support.UserInfoFiller} 统一回填。
 * Lombok 的 {@code @Data} 已生成这些方法，实体只需声明 implements。
 */
public interface UserOwned {

    Long getUserId();

    void setUsername(String username);

    void setNickname(String nickname);

    void setAvatar(String avatar);
}
