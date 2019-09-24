package com.dh.shiroredis.mapper;

import com.dh.shiroredis.model.User;
import org.apache.ibatis.annotations.Param;
public interface UserMapper {
    User findByUsername(@Param("username") String username);
}
