package com.dh.shiroredis.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class User implements Serializable {

    private Integer uid;
    private String username;
    private String password;
    private Set<Role> roles = new HashSet<>();
}
