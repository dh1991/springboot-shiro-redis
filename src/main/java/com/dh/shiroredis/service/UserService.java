package com.dh.shiroredis.service;

import com.dh.shiroredis.model.User;

public interface UserService {
    User findByUsername(String username);
}
