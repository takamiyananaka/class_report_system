package com.xuegongbu.vo;

import lombok.Data;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
}
