package com.lune.service;

import com.lune.common.PageResult;
import com.lune.dto.ChangePasswordRequest;
import com.lune.dto.UpdateProfileRequest;
import com.lune.entity.User;

public interface UserService {
    PageResult<User> listUsers(int page, int size);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void updateRole(Long id, String role);
    User getCurrentUser();
    User getPublicProfile(Long userId);
    User updateProfile(UpdateProfileRequest req);
    void changePassword(ChangePasswordRequest req);
    void sendDeleteCode();
    void deleteAccount(String code);
}
