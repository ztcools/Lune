package com.lune.service.support;

import com.lune.entity.User;
import com.lune.entity.UserOwned;
import com.lune.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 回填作者信息（username / nickname / avatar）。
 *
 * 树洞、随笔、记录、评论四个 ServiceImpl 此前各自维护一份逐字相同的
 * populateUserInfo —— 同一套「避免 N+1」的批量查询逻辑抄了四遍，
 * 改一处就会漏三处。统一到这里：列表走一次 IN 查询，单条走一次主键查询。
 */
@Component
public class UserInfoFiller {

    private final UserMapper userMapper;

    public UserInfoFiller(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 批量回填：一条 IN 查询搞定，不按行查库。 */
    public void fill(List<? extends UserOwned> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> userIds = list.stream()
                .map(UserOwned::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        for (UserOwned item : list) {
            apply(item, userMap.get(item.getUserId()));
        }
    }

    /** 单条回填：新建 / 详情接口用。userId 为空或 0（匿名）时什么都不做。 */
    public void fillOne(UserOwned item) {
        if (item == null || item.getUserId() == null || item.getUserId() <= 0) return;
        apply(item, userMapper.selectById(item.getUserId()));
    }

    private void apply(UserOwned item, User user) {
        if (user == null) return;
        item.setUsername(user.getUsername());
        item.setNickname(user.getNickname());
        item.setAvatar(user.getAvatar());
    }
}
