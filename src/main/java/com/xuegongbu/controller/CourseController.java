package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.Course;
import com.xuegongbu.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

@Slf4j
@RestController
@RequestMapping("/course")
@Tag(name = "课程管理", description = "课程相关接口")
public class CourseController {

    @Autowired
    private CourseService courseService;



    /**
     * 创建课程
     */
    @PostMapping("/add")
    @Operation(summary = "创建课程", description = "教师创建新课程，教师工号从登录状态获取")
    public Result<Course> addCourse(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课程信息") @RequestBody Course course) {
        log.info("创建课程，课程信息：{}", course);
        
        // 获取当前登录教师的工号
        if (!StpUtil.isLogin()) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        String teacherNo = null;
        try {
            // 优先从会话中获取完整的用户信息
            SaSession session = StpUtil.getSession();
            com.xuegongbu.domain.Teacher teacher = (com.xuegongbu.domain.Teacher) session.get("userInfo");
            if (teacher != null) {
                teacherNo = teacher.getTeacherNo();
            } else {
                // 回退到原来的逻辑
                Object loginId = StpUtil.getLoginId();
                if (loginId instanceof String) {
                    teacherNo = (String) loginId;
                }
            }
        } catch (Exception e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 设置教师工号
        course.setTeacherNo(teacherNo);
        log.info("设置课程教师工号: {}", teacherNo);
        
        courseService.save(course);
        log.info("创建课程完成，课程ID：{}", course.getId());
        return Result.success(course);
    }

    /**
     * 更新课程
     */
    @PutMapping("/update")
    @Operation(summary = "更新课程", description = "教师更新课程信息")
    public Result<String> updateCourse(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课程信息") @RequestBody Course course) {
        log.info("更新课程，课程ID：{}，课程信息：{}", course.getId(), course);
        courseService.updateById(course);
        log.info("更新课程完成");
        return Result.success("更新成功");
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除课程", description = "教师删除课程")
    public Result<String> deleteCourse(@Parameter(description = "课程ID") @PathVariable String id) {
        log.info("删除课程，课程ID：{}", id);
        courseService.removeById(id);
        log.info("删除课程完成");
        return Result.success("删除成功");
    }

    /**
     * 查询课程详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "查询课程详情", description = "根据课程ID查询课程详情")
    public Result<Course> getCourse(@Parameter(description = "课程ID") @PathVariable String id) {
        log.info("查询课程详情，课程ID：{}", id);
        Course course = courseService.getById(id);
        log.info("查询课程详情完成");
        return Result.success(course);
    }

    /**
     * 查询教师的所有课程
     */
    @GetMapping("/list")
    @Operation(summary = "查询教师的所有课程", description = "查询当前登录教师的所有课程")
    public Result<List<Course>> listCourses(@Parameter(description = "教师工号") @RequestParam(required = false) String teacherNo) {
        // 如果没有指定教师工号，使用当前登录教师的工号
        if (teacherNo == null) {
            if (StpUtil.isLogin()) {
                try {
                    // 优先从会话中获取完整的用户信息
                    SaSession session = StpUtil.getSession();
                    com.xuegongbu.domain.Teacher teacher = (com.xuegongbu.domain.Teacher) session.get("userInfo");
                    if (teacher != null) {
                        teacherNo = teacher.getTeacherNo();
                    } else {
                        // 回退到原来的逻辑
                        Object loginId = StpUtil.getLoginId();
                        if (loginId instanceof String) {
                            teacherNo = (String) loginId;
                        }
                    }
                } catch (Exception e) {
                    log.error("无法解析教师工号: {}", e.getMessage());
                    return Result.error("无法获取当前登录用户信息");
                }
            }
        }
        
        log.info("查询教师课程列表，教师工号：{}", teacherNo);
        List<Course> courses = courseService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                .eq(teacherNo != null, Course::getTeacherNo, teacherNo)
        );
        log.info("查询教师课程列表完成，共{}条记录", courses.size());
        return Result.success(courses);
    }
}