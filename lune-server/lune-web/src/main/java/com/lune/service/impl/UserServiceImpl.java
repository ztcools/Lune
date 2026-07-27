package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.dto.ChangePasswordRequest;
import com.lune.dto.UpdateProfileRequest;
import com.lune.entity.User;
import com.lune.mapper.UserMapper;
import com.lune.service.EmailService;
import com.lune.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Claims claims)) {
            throw new BusinessException("未登录");
        }
        return claims.get("userId", Long.class);
    }

    @Override
    public User getPublicProfile(Long userId) {
        var user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        var user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateProfile(UpdateProfileRequest req) {
        Long userId = getCurrentUserId();
        var user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getBirthday() != null) user.setBirthday(LocalDate.parse(req.getBirthday()));
        if (req.getSignature() != null) user.setSignature(req.getSignature());
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public void changePassword(ChangePasswordRequest req) {
        Long userId = getCurrentUserId();
        var user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void sendDeleteCode() {
        Long userId = getCurrentUserId();
        var user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if ("ADMIN".equals(user.getRole())) throw new BusinessException("管理员不能注销账号");
        emailService.sendVerificationCode(user.getEmail());
    }

    @Override
    public void deleteAccount(String code) {
        Long userId = getCurrentUserId();
        var user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if ("ADMIN".equals(user.getRole())) throw new BusinessException("管理员不能注销账号");
        if (!emailService.verifyCode(user.getEmail(), code)) {
            throw new BusinessException("验证码错误或已过期");
        }
        emailService.deleteCode(user.getEmail());
        userMapper.deleteById(userId);
    }

    @Override
    public PageResult<User> listUsers(int page, int size) {
        var wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        var result = userMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
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
        exist.setPassword(null);
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
