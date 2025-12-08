package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Course;
import com.xuegongbu.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/course")
@Api(tags = "课程管理")
public class CourseController {

    @Autowired
    private CourseService courseService;

}
