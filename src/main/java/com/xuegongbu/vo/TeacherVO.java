package com.xuegongbu.vo;

import lombok.Data;

@Data
public class TeacherVO {
    private Long id;
    private String username;
    private String realName;
    private String teacherNo;
    private String phone;
    private String email;
    private String department;
    private Integer status;
}
