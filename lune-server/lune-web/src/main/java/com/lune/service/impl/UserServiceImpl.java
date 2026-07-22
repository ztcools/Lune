package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.User;
import com.lune.mapper.UserMapper;
import com.lune.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<User> listUsers(int page, int size) {
        var wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        var result = userMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public User updateUser(Long id, User user) {
        var exist = userMapper.selectById(id);
        if (exist == null) throw new BusinessException("用户不存在");
        exist.setNickname(user.getNickname());
        exist.setEmail(user.getEmail());
        exist.setAvatar(user.getAvatar());
        userMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteUser(Long id) {
        var user = userMapper.selectById(id);
        if (user != null && "ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void updateRole(Long id, String role) {
        var user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setRole(role);
        userMapper.updateById(user);
    }
}
