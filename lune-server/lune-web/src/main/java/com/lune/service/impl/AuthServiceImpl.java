package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.common.BusinessException;
import com.lune.common.Result;
import com.lune.dto.*;
import com.lune.entity.User;
import com.lune.mapper.UserMapper;
import com.lune.security.JwtTokenProvider;
import com.lune.service.AuthService;
import com.lune.service.EmailService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String[] DEFAULT_NICKNAMES = {
        "小月亮", "星辰旅人", "微风轻拂", "花开半夏", "清茶煮酒",
        "云端漫步", "细雨微风", "山间清风", "海边落日", "林间小鹿",
        "北极星的眼泪", "柠檬味的夏天", "暖阳如初", "夜雨声烦", "风吹麦浪"
    };

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider, RedisTemplate<String, String> redisTemplate,
                           EmailService emailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    @Override
    public Result<LoginResponse> login(LoginRequest request) {
        String account = request.getAccount();
        var user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account));
        if (user == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, account));
        }
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        return Result.success(buildResponse(user, token));
    }

    @Override
    public Result<LoginResponse> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }
        if (!emailService.verifyCode(request.getEmail(), request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        var exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (exist != null) {
            throw new BusinessException("邮箱已被注册");
        }
        Random rnd = new Random();
        var user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(DEFAULT_NICKNAMES[rnd.nextInt(DEFAULT_NICKNAMES.length)]);
        user.setEmail(request.getEmail());
        user.setAvatar(null);
        user.setGender("保密");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        emailService.deleteCode(request.getEmail());
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        return Result.success(buildResponse(user, token));
    }

    @Override
    public Result<Void> sendCode(SendCodeRequest request) {
        String email = request.getEmail();
        var exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (exist != null) {
            throw new BusinessException("邮箱已被注册");
        }
        emailService.sendVerificationCode(email);
        return Result.success();
    }

    @Override
    public Result<Void> logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null) {
            long expiration = jwtTokenProvider.parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
            if (expiration > 0) {
                redisTemplate.opsForValue().set("token:blacklist:" + token, "1", expiration, TimeUnit.MILLISECONDS);
            }
        }
        return Result.success();
    }

    private LoginResponse buildResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .signature(user.getSignature())
                .role(user.getRole())
                .build();
    }
}
