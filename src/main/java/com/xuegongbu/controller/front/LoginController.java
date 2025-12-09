package com.xuegongbu.controller.front;

import com.xuegongbu.service.TeacherService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/front")
@Api("登录管理")
public class LoginController {

    @Autowired
    private TeacherService teacherService;


}
