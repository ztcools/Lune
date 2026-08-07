package com.lune.controller;

import com.lune.common.Result;
import com.lune.dto.ChangePasswordRequest;
import com.lune.dto.SendCodeRequest;
import com.lune.dto.UpdateProfileRequest;
import com.lune.entity.User;
import com.lune.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{userId}")
    public Result<User> getPublicProfile(@PathVariable Long userId) {
        return Result.success(userService.getPublicProfile(userId));
    }

    @GetMapping("/profile")
    public Result<User> getProfile() {
        return Result.success(userService.getCurrentUser());
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return Result.success(userService.updateProfile(req));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(req);
        return Result.success();
    }

    @PostMapping("/send-delete-code")
    public Result<Void> sendDeleteCode() {
        userService.sendDeleteCode();
        return Result.success();
    }

    @DeleteMapping("/account")
    public Result<Void> deleteAccount(@RequestBody Map<String, String> body) {
        userService.deleteAccount(body.get("code"));
        return Result.success();
    }
}
