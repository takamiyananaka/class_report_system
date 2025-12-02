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
    /**
     * 身份字段：1=只是教师，2=教师且是辅导员
     */
    private Integer identity;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
