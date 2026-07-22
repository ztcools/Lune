package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.User;

public interface UserService {
    PageResult<User> listUsers(int page, int size);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void updateRole(Long id, String role);
}
