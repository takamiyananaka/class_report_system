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

    /**
     * 根据教师ID查询教师信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据教师ID查询教师信息")
    public Result<Teacher> getById(@PathVariable Long id) {
        log.info("查询教师请求，ID：{}", id);
        Teacher teacher = teacherService.findById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }
        return Result.success(teacher);
    }
}
