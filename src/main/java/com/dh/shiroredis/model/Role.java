package com.dh.shiroredis.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class Role implements Serializable {
    private Integer rid;
    private String rname;
    private Set<Permission> permissions = new HashSet<>();
    private Set<User> users = new HashSet<>();
}
