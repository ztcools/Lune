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

    /**
     * 站长公开名片（未登录可访问，简历页/首页署名使用）。
     *
     * 该接口只暴露站长（role=ADMIN）本人：原实现对任意 userId 都返回整个 User，
     * 只清了 password，于是遍历自增 id 就能拖走全站用户的邮箱与角色。
     * 简历页确实要展示站长邮箱作为联系方式，所以这里保留 email，
     * 但把与展示无关的账号字段（role/status/deleted/时间戳）一并清掉——
     * 少给一个字段，就少一分账号枚举与撞库的输入。
     */
    @Override
    public User getPublicProfile(Long userId) {
        var user = userMapper.selectById(userId);
        // 非站长一律按「不存在」处理，避免以是否报错来判定 id 是否被占用
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        user.setDeleted(null);
        user.setCreateTime(null);
        user.setUpdateTime(null);
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
        // 大小写一并挡住：role 曾是自由文本，历史数据里可能存着 'admin'
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("不能删除管理员");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void updateRole(Long id, String role) {
        // role 直接来自请求参数，且 JwtAuthFilter 是拿 "ROLE_" + role 当权限用的。
        // 不做白名单的话，写进去一个 'admin' 或拼错的字符串，账号既不是 ADMIN
        // 也不是 USER —— 权限判断静默失效，且只能改库救回来。
        String normalized = role == null ? "" : role.trim().toUpperCase();
        if (!"ADMIN".equals(normalized) && !"USER".equals(normalized)) {
            throw new BusinessException("角色只能是 ADMIN 或 USER");
        }
        var user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        boolean wasAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (wasAdmin && "USER".equals(normalized)) {
            // 自降级会让当前这个人立刻失去后台入口；降掉最后一个管理员则全站无人能进后台。
            // 两种情况都只能改库恢复，所以在这里拦住。
            if (id.equals(getCurrentUserId())) {
                throw new BusinessException("不能取消自己的管理员权限");
            }
            Long adminCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .apply("UPPER(role) = 'ADMIN'"));
            if (adminCount != null && adminCount <= 1) {
                throw new BusinessException("至少需要保留一个管理员");
            }
        }

        user.setRole(normalized);
        userMapper.updateById(user);
    }
}
