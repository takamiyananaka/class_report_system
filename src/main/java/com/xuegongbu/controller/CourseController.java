package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Attendance;
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

    /**
     * 查询课程考勤记录
     */
    @GetMapping("/queryAttendance")
    @ApiOperation(value = "查询课程考勤记录", notes = "查询课程考勤记录")
    public Result<List<Attendance>> queryAttendanceByCourseId(
            @RequestParam(value = "courseId", required = true) Long courseId) {
        log.info("查询课程考勤记录，课程ID：{}", courseId);
        List<Attendance> attendanceList = courseService.queryAttendanceByCourseId(courseId);
        log.info("查询课程考勤记录完成，结果：{}", attendanceList);
        return Result.success(attendanceList);
    }

    /**
     * 手动考勤
     */
    @PostMapping("/manualAttendance")
    @ApiOperation(value = "手动考勤", notes = "手动考勤")
    public Result<String> manualAttendance(
            @RequestParam(value = "courseId", required = true) Long courseId){
        log.info("手动考勤，课程ID：{}", courseId);
        courseService.manualAttendance(courseId);
        log.info("手动考勤完成");
        return Result.success("考勤记录生成完成");
    }
}
