package com.xuegongbu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.dto.CourseScheduleVO;
import com.xuegongbu.dto.CourseScheduleWithClassIdQueryDTO;
import com.xuegongbu.service.CourseScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

/**
 * 课表管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/courseSchedule")
@Tag(name = "课表管理", description = "课表相关接口")
public class CourseScheduleController {

    @Autowired
    private CourseScheduleService courseScheduleService;

    /**
     * Excel导入课表
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Excel导入课表", description = "通过上传Excel文件批量导入课表数据。教师ID将根据当前登录用户自动填充。Excel格式要求：第一行为表头，列顺序为：课程名称、课程号、课序号、班级名称、星期几(1-7)、周次范围(格式：x-x周)、开始节次(1-12)、结束节次(1-12)、教室")
    public Result<Map<String, Object>> importFromExcel(
            @Parameter(description = "Excel文件", required = true, 
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("开始导入课表，文件名：{}", file.getOriginalFilename());
            Map<String, Object> result = courseScheduleService.importFromExcel(file);
            log.info("课表导入完成：{}", result.get("message"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("课表导入失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询课表
     * 默认查询当前登录教师的课表，也可以通过参数指定教师ID或班级名称查询
     */
    @PostMapping("/query")
    @Operation(summary = "按老师分页查询课表", description = "分页查询课表，默认查询当前登录教师的课表。教师工号默认从后端获取")
    public Result<Page<CourseScheduleVO>> query(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "查询条件") @RequestBody CourseScheduleQueryDTO queryDTO) {
        //如果角色身份为教师，则使用当前教师工号查询
        if (StpUtil.hasRole("teacher")) {
            if (StpUtil.isLogin()) {
                try {
                    String currentTeacherNo = null;
                    Object loginId = StpUtil.getLoginId();
                    if (loginId instanceof String) {
                        currentTeacherNo = (String) loginId;
                    }

                    if (currentTeacherNo != null) {
                        queryDTO.setTeacherNo(currentTeacherNo);
                        log.info("使用当前登录教师工号查询课表: {}", currentTeacherNo);
                    } else {
                        log.warn("无法解析当前登录教师工号，将查询所有课表");
                    }
                } catch (Exception e) {
                    log.warn("无法解析当前登录教师工号，将查询所有课表: {}", e.getMessage());
                }
            }
        }
        log.info("查询课表请求，参数：{}", queryDTO);
        Page<CourseScheduleVO> result = courseScheduleService.queryPage(queryDTO);
        log.info("查询课表完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 按班分页查询课表
     */
    @PostMapping("/queryByClass")
    @Operation(summary = "按班分页查询课表", description = "按班分页查询课表,id为班级id")
    public Result<Page<CourseSchedule>> queryByClass(@RequestBody CourseScheduleWithClassIdQueryDTO queryDTO) {
        Page<CourseSchedule> result = courseScheduleService.queryByClass(queryDTO.getClassId(),queryDTO.getPageNum(),queryDTO.getPageSize());
        log.info("查询课表完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 下载课表导入模板
     */
    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载课表导入模板", description = "下载Excel格式的课表导入模板文件")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            log.info("下载课表导入模板");
            courseScheduleService.downloadTemplate(response);
            log.info("下载课表导入模板完成");
        } catch (Exception e) {
            log.error("下载课表导入模板失败", e);
            throw new com.xuegongbu.common.exception.BusinessException("下载模板失败: " + e.getMessage());
        }
    }

    /**
     * 为课程添加班级，以列表的形式
     */
    @PostMapping("/addClass/{id}")
    @Operation(summary = "为课程添加班级", description = "为课程添加班级，以列表的形式")
    public Result<String> addClass(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "班级列表") @RequestBody List<String> classList, @Parameter(description = "课程ID") @PathVariable String id) {
        log.info("为课程添加班级，班级列表：{}", classList);
       courseScheduleService.addClass(classList,id);
        log.info("为课程添加班级完成");
        return Result.success("添加班级成功");
    }

    /**
     * 创建课表
     */
    @PostMapping("/add")
    @Operation(summary = "创建课表", description = "教师创建新课表，教师工号从登录状态获取，ID自动生成")
    public Result<CourseSchedule> addCourseSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课表信息") @RequestBody CourseSchedule courseSchedule) {
        log.info("创建课表，课表信息：{}", courseSchedule);

        // 验证必填字段
        if (courseSchedule.getCourseName() == null || courseSchedule.getCourseName().trim().isEmpty()) {
            return Result.error("课程名称不能为空");
        }
        if (courseSchedule.getWeekday() == null || courseSchedule.getWeekday().trim().isEmpty()) {
            return Result.error("星期几不能为空");
        }
        if (!isValidWeekday(courseSchedule.getWeekday())) {
            return Result.error("星期几格式不正确，应为：星期一、星期二、星期三、星期四、星期五、星期六、星期日");
        }
        if (courseSchedule.getWeekRange() == null || courseSchedule.getWeekRange().trim().isEmpty()) {
            return Result.error("周次范围不能为空");
        }
        if (courseSchedule.getStartPeriod() == null || courseSchedule.getStartPeriod() < 1 || courseSchedule.getStartPeriod() > 12) {
            return Result.error("开始节次必须是1-12之间的数字");
        }
        if (courseSchedule.getEndPeriod() == null || courseSchedule.getEndPeriod() < 1 || courseSchedule.getEndPeriod() > 12) {
            return Result.error("结束节次必须是1-12之间的数字");
        }
        if (courseSchedule.getEndPeriod() < courseSchedule.getStartPeriod()) {
            return Result.error("结束节次必须大于或等于开始节次");
        }
        if (courseSchedule.getClassroom() == null || courseSchedule.getClassroom().trim().isEmpty()) {
            return Result.error("教室不能为空");
        }
        
        // ID会由MyBatis-Plus自动生成（雪花算法）
        courseScheduleService.save(courseSchedule);
        log.info("创建课表完成，课表ID：{}", courseSchedule.getId());
        return Result.success(courseSchedule);
    }

    /**
     * 更新课表
     */
    @PutMapping("/update")
    @Operation(summary = "更新课表", description = "教师更新课表信息，通过课程名称和班级名称定位，只能更新自己的课表")
    public Result<String> updateCourseSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课表信息") @RequestBody CourseSchedule courseSchedule) {
        log.info("更新课表，课程名称：{}，课表信息：{}", 
                courseSchedule.getCourseName(), courseSchedule);
        
        if (courseSchedule.getCourseName() == null || courseSchedule.getCourseName().trim().isEmpty()) {
            return Result.error("课程名称不能为空");
        }
        if (courseSchedule.getWeekday() == null || courseSchedule.getWeekday().trim().isEmpty()) {
            return Result.error("星期几不能为空");
        }
        if (!isValidWeekday(courseSchedule.getWeekday())) {
            return Result.error("星期几格式不正确，应为：星期一、星期二、星期三、星期四、星期五、星期六、星期日");
        }

        // 根据课程名称查询课表（班级信息现在通过关联表获取）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseSchedule.getCourseName().trim());
        
        CourseSchedule existing = courseScheduleService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("课表不存在或无权限修改");
        }
        
        // 设置ID以便更新
        courseSchedule.setId(existing.getId());
        
        courseScheduleService.updateById(courseSchedule);
        log.info("更新课表完成");
        return Result.success("更新成功");
    }

    /**
     * 删除课表
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除课表", description = "教师删除课表，通过课程名称和班级名称定位，只能删除自己的课表")
    public Result<String> deleteCourseSchedule(
            @Parameter(description = "课程名称", required = true) @RequestParam(value = "courseName", required = true) String courseName,
            @Parameter(description = "班级名称", required = true) @RequestParam(value = "className", required = true) String className) {
        log.info("删除课表，课程名称：{}，班级名称：{}", courseName, className);
        
        // 获取当前登录教师的工号
        if (!StpUtil.isLogin()) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        String teacherNo = null;
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof String) {
                teacherNo = (String) loginId;
            }
        } catch (Exception e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 根据课程名称查询课表（班级信息现在通过关联表获取）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseName.trim());
        
        CourseSchedule existing = courseScheduleService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("课表不存在或无权限删除");
        }
        
        courseScheduleService.removeById(existing.getId());
        log.info("删除课表完成");
        return Result.success("删除成功");
    }

    /**
     * 查询课表详情
     */
    @GetMapping("/get")
    @Operation(summary = "查询课表详情", description = "根据课程名称和班级名称查询课表详情，只能查询自己的课表")
    public Result<CourseSchedule> getCourseSchedule(
            @Parameter(description = "课程名称", required = true) @RequestParam(value = "courseName", required = true) String courseName,
            @Parameter(description = "班级名称", required = true) @RequestParam(value = "className", required = true) String className) {
        log.info("查询课表详情，课程名称：{}，班级名称：{}", courseName, className);
        
        // 获取当前登录教师的工号
        if (!StpUtil.isLogin()) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        String teacherNo = null;
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof String) {
                teacherNo = (String) loginId;
            }
        } catch (Exception e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 根据课程名称查询课表（班级信息现在通过关联表获取）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseName.trim());
        
        CourseSchedule courseSchedule = courseScheduleService.getOne(queryWrapper);
        if (courseSchedule == null) {
            return Result.error("课表不存在或无权限查看");
        }
        
        log.info("查询课表详情完成");
        return Result.success(courseSchedule);
    }

    /**
     * 根据ID查询课表详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询课表详情", description = "根据课表ID查询课表详情")
    public Result<CourseSchedule> getCourseScheduleById(@Parameter(description = "课表ID") @PathVariable String id) {
        log.info("根据ID查询课表详情，课表ID：{}", id);
        
        CourseSchedule courseSchedule = courseScheduleService.getById(id);
        if (courseSchedule == null) {
            return Result.error("课表不存在");
        }
        
        log.info("查询课表详情完成");
        return Result.success(courseSchedule);
    }

    /**
     * 根据ID更新课表
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "根据ID更新课表", description = "通过课表ID更新课表信息")
    public Result<String> updateCourseScheduleById(@Parameter(description = "课表ID") @PathVariable String id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课表信息") @RequestBody CourseSchedule courseSchedule) {
        log.info("根据ID更新课表，课表ID：{}，课表信息：{}", id, courseSchedule);
        
        CourseSchedule existing = courseScheduleService.getById(id);
        if (existing == null) {
            return Result.error("课表不存在");
        }
        
        // 设置ID以便更新
        courseSchedule.setId(id);
        
        courseScheduleService.updateById(courseSchedule);
        log.info("更新课表完成");
        return Result.success("更新成功");
    }

    /**
     * 根据ID删除课表
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "根据ID删除课表", description = "通过课表ID删除课表")
    public Result<String> deleteCourseScheduleById(@Parameter(description = "课表ID") @PathVariable String id) {
        log.info("根据ID删除课表，课表ID：{}", id);
        
        CourseSchedule existing = courseScheduleService.getById(id);
        if (existing == null) {
            return Result.error("课表不存在");
        }
        
        courseScheduleService.removeById(id);
        log.info("删除课表完成");
        return Result.success("删除成功");
    }

    /**
     * 验证星期几格式是否正确
     * @param weekday 星期几（汉字格式）
     * @return 是否有效
     */
    private boolean isValidWeekday(String weekday) {
        if (weekday == null) {
            return false;
        }
        String[] validWeekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        for (String validWeekday : validWeekdays) {
            if (validWeekday.equals(weekday.trim())) {
                return true;
            }
        }
        return false;
    }
}