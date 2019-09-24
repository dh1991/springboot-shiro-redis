package com.dh.shiroredis.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Permission implements Serializable {
    private Integer pid;
    private String pname;
    private String url;
}
