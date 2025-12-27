package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Course;
import com.xuegongbu.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/course")
@Tag(name = "课程班级关联管理", description = "课程班级关联相关接口")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 分页查询课程班级关联
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询课程班级关联", description = "分页查询课程班级关联，支持多条件查询。可通过courseId、classId等参数进行过滤查询。不提供任何条件则查询全部")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Course>> query(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "查询条件") @RequestBody com.xuegongbu.dto.CourseQueryDTO queryDTO) {
        log.info("查询课程班级关联请求，参数：{}", queryDTO);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Course> result = courseService.queryPage(queryDTO);
        log.info("查询课程班级关联完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 创建课程班级关联
     */
    @PostMapping("/add")
    @Operation(summary = "创建课程班级关联", description = "创建新的课程班级关联")
    public Result<Course> addCourse(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课程班级关联信息") @RequestBody Course course) {
        log.info("创建课程班级关联，关联信息：{}", course);
        courseService.save(course);
        log.info("创建课程班级关联完成，关联ID：{}", course.getId());
        return Result.success(course);
    }

    /**
     * 更新课程班级关联
     */
    @PutMapping("/update")
    @Operation(summary = "更新课程班级关联", description = "更新课程班级关联信息")
    public Result<String> updateCourse(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课程班级关联信息") @RequestBody Course course) {
        log.info("更新课程班级关联，关联ID：{}，关联信息：{}", course.getId(), course);
        courseService.updateById(course);
        log.info("更新课程班级关联完成");
        return Result.success("更新成功");
    }

    /**
     * 删除课程班级关联
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除课程班级关联", description = "删除课程班级关联")
    public Result<String> deleteCourse(@Parameter(description = "关联ID") @PathVariable String id) {
        log.info("删除课程班级关联，关联ID：{}", id);
        courseService.removeById(id);
        log.info("删除课程班级关联完成");
        return Result.success("删除成功");
    }

    /**
     * 查询课程班级关联详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "查询课程班级关联详情", description = "根据关联ID查询课程班级关联详情")
    public Result<Course> getCourse(@Parameter(description = "关联ID") @PathVariable String id) {
        log.info("查询课程班级关联详情，关联ID：{}", id);
        Course course = courseService.getById(id);
        log.info("查询课程班级关联详情完成");
        return Result.success(course);
    }

    /**
     * 查询指定课程ID的所有班级关联
     */
    @GetMapping("/list")
    @Operation(summary = "查询指定课程的所有班级关联", description = "查询指定课程ID的所有班级关联")
    public Result<List<Course>> listCourses(@Parameter(description = "课程ID") @RequestParam(required = false) String courseId) {
        log.info("查询课程班级关联列表，课程ID：{}", courseId);
        List<Course> courses = courseService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                .eq(courseId != null, Course::getCourseId, courseId)
        );
        log.info("查询课程班级关联列表完成，共{}条记录", courses.size());
        return Result.success(courses);
    }
}