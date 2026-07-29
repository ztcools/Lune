package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.entity.TreeHole;
import com.lune.mapper.TreeHoleMapper;
import com.lune.mapper.UserMapper;
import com.lune.service.TreeHoleService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TreeHoleServiceImpl implements TreeHoleService {

    private final TreeHoleMapper treeHoleMapper;
    private final UserMapper userMapper;

    public TreeHoleServiceImpl(TreeHoleMapper treeHoleMapper, UserMapper userMapper) {
        this.treeHoleMapper = treeHoleMapper;
        this.userMapper = userMapper;
    }

    private void populateUserInfo(java.util.List<TreeHole> list) {
        Set<Long> userIds = list.stream()
                .map(TreeHole::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        var users = userMapper.selectBatchIds(userIds);
        Map<Long, com.lune.entity.User> userMap = users.stream()
                .collect(Collectors.toMap(com.lune.entity.User::getId, Function.identity()));
        for (TreeHole t : list) {
            var u = userMap.get(t.getUserId());
            if (u != null) {
                t.setUsername(u.getUsername());
                t.setNickname(u.getNickname());
                t.setAvatar(u.getAvatar());
            }
        }
    }

    @Override
    public PageResult<TreeHole> listTreeHoles(int page, int size) {
        var wrapper = new LambdaQueryWrapper<TreeHole>()
                .eq(TreeHole::getStatus, 1)
                .orderByDesc(TreeHole::getCreateTime);
        var result = treeHoleMapper.selectPage(new Page<>(page, size), wrapper);
        populateUserInfo(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public TreeHole createTreeHole(TreeHole treeHole) {
        treeHole.setStatus(1);
        // XSS 清洗：树洞内容为纯文本弹幕
        treeHole.setContent(com.lune.security.XssSanitizer.clean(treeHole.getContent(), 200));
        treeHoleMapper.insert(treeHole);
        if (treeHole.getUserId() != null) {
            var user = userMapper.selectById(treeHole.getUserId());
            if (user != null) {
                treeHole.setUsername(user.getUsername());
                treeHole.setNickname(user.getNickname());
                treeHole.setAvatar(user.getAvatar());
            }
        }
        return treeHole;
    }

    @Override
    public void deleteTreeHole(Long id) {
        treeHoleMapper.deleteById(id);
    }
}
