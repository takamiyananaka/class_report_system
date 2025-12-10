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

    /**
     * 创建课程
     */
    @PostMapping("/add")
    @ApiOperation(value = "创建课程", notes = "教师创建新课程")
    public Result<Course> addCourse(@RequestBody Course course) {
        log.info("创建课程，课程信息：{}", course);
        courseService.save(course);
        log.info("创建课程完成，课程ID：{}", course.getId());
        return Result.success(course);
    }

    /**
     * 更新课程
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新课程", notes = "教师更新课程信息")
    public Result<String> updateCourse(@RequestBody Course course) {
        log.info("更新课程，课程ID：{}，课程信息：{}", course.getId(), course);
        courseService.updateById(course);
        log.info("更新课程完成");
        return Result.success("更新成功");
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除课程", notes = "教师删除课程")
    public Result<String> deleteCourse(@PathVariable Long id) {
        log.info("删除课程，课程ID：{}", id);
        courseService.removeById(id);
        log.info("删除课程完成");
        return Result.success("删除成功");
    }

    /**
     * 查询课程详情
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "查询课程详情", notes = "根据课程ID查询课程详情")
    public Result<Course> getCourse(@PathVariable Long id) {
        log.info("查询课程详情，课程ID：{}", id);
        Course course = courseService.getById(id);
        log.info("查询课程详情完成");
        return Result.success(course);
    }

    /**
     * 查询教师的所有课程
     */
    @GetMapping("/list")
    @ApiOperation(value = "查询教师的所有课程", notes = "查询当前登录教师的所有课程")
    public Result<List<Course>> listCourses(@RequestParam(required = false) Long teacherId) {
        log.info("查询教师课程列表，教师ID：{}", teacherId);
        List<Course> courses = courseService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                .eq(teacherId != null, Course::getTeacherId, teacherId)
        );
        log.info("查询教师课程列表完成，共{}条记录", courses.size());
        return Result.success(courses);
    }
}
