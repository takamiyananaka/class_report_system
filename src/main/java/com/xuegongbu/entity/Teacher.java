package com.xuegongbu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Teacher {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String teacherNo;
    private String phone;
    private String email;
    private String department;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
