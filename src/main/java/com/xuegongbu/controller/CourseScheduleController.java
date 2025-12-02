package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.dto.CourseScheduleRequest;
import com.xuegongbu.entity.CourseSchedule;
import com.xuegongbu.service.CourseScheduleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/course-schedule")
public class CourseScheduleController {

    @Autowired
    private CourseScheduleService courseScheduleService;

    /**
     * 创建课表
     */
    @PostMapping
    public Result<CourseSchedule> create(@Valid @RequestBody CourseScheduleRequest request) {
        log.info("创建课表请求：{}", request.getCourseName());
        CourseSchedule courseSchedule = new CourseSchedule();
        BeanUtils.copyProperties(request, courseSchedule);
        CourseSchedule created = courseScheduleService.create(courseSchedule);
        return Result.success(created);
    }

    /**
     * 根据ID查询课表
     */
    @GetMapping("/{id}")
    public Result<CourseSchedule> getById(@PathVariable Long id) {
        log.info("查询课表请求，ID：{}", id);
        CourseSchedule courseSchedule = courseScheduleService.findById(id);
        if (courseSchedule == null) {
            return Result.error("课表不存在");
        }
        return Result.success(courseSchedule);
    }

    /**
     * 查询课表列表（支持分页和条件查询）
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) Integer weekday,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String schoolYear,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询课表列表请求");
        Map<String, Object> result = courseScheduleService.findList(courseName, teacherId, className, 
                weekday, semester, schoolYear, page, size);
        return Result.success(result);
    }

    /**
     * 更新课表
     */
    @PutMapping("/{id}")
    public Result<CourseSchedule> update(@PathVariable Long id, @Valid @RequestBody CourseScheduleRequest request) {
        log.info("更新课表请求，ID：{}", id);
        CourseSchedule existing = courseScheduleService.findById(id);
        if (existing == null) {
            return Result.error("课表不存在");
        }
        CourseSchedule courseSchedule = new CourseSchedule();
        BeanUtils.copyProperties(request, courseSchedule);
        CourseSchedule updated = courseScheduleService.update(id, courseSchedule);
        return Result.success(updated);
    }

    /**
     * 删除课表
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除课表请求，ID：{}", id);
        CourseSchedule existing = courseScheduleService.findById(id);
        if (existing == null) {
            return Result.error("课表不存在");
        }
        boolean deleted = courseScheduleService.delete(id);
        if (deleted) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    /**
     * 查询某教师的课表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<CourseSchedule>> getByTeacherId(@PathVariable Long teacherId) {
        log.info("查询教师课表请求，教师ID：{}", teacherId);
        List<CourseSchedule> list = courseScheduleService.findByTeacherId(teacherId);
        return Result.success(list);
    }

    /**
     * 查询某班级的课表
     */
    @GetMapping("/class/{className}")
    public Result<List<CourseSchedule>> getByClassName(@PathVariable String className) {
        log.info("查询班级课表请求，班级名称：{}", className);
        List<CourseSchedule> list = courseScheduleService.findByClassName(className);
        return Result.success(list);
    }
}
