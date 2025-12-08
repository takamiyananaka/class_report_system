package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.service.TeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/teacher")
@Api(tags = "教师管理")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

}
