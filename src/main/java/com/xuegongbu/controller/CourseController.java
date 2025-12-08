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

    /**
     * 通过Excel导入课程
     */
    @PostMapping("/import")
    @ApiOperation("通过Excel导入课程")
    public Result<Map<String, Object>> importCourses(@RequestParam("file") MultipartFile file) {
        log.info("接收到Excel导入请求，文件名：{}", file.getOriginalFilename());
        
        try {
            int count = courseService.importCoursesFromExcel(file);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("message", "成功导入" + count + "条课程数据");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入课程失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询课程
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询课程")
    public Result<Course> getById(@PathVariable Long id) {
        log.info("查询课程请求，ID：{}", id);
        Course course = courseService.findById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        return Result.success(course);
    }

    /**
     * 查询所有课程
     */
    @GetMapping("/list")
    @ApiOperation("查询所有课程")
    public Result<List<Course>> list() {
        log.info("查询所有课程");
        List<Course> courses = courseService.findAll();
        return Result.success(courses);
    }
}
