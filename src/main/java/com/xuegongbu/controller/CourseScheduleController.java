package com.xuegongbu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.service.CourseScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
    @Operation(summary = "Excel导入课表", description = "通过上传Excel文件批量导入课表数据。教师ID将根据当前登录用户自动填充。Excel格式要求：第一行为表头，列顺序为：课程名称、班级名称、星期几(1-7)、开始时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式)、结束时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式)、教室、学期、学年")
    public Result<Map<String, Object>> importFromExcel(
            @Parameter(description = "Excel文件", required = true, 
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("开始导入课表，文件名：{}", file.getOriginalFilename());
            
            // 获取当前登录教师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return Result.error("未登录或登录已过期，请重新登录");
            }
            
            Long teacherNo = null;
            try {
                Object principal = authentication.getPrincipal();
                // principal现在是teacherNo (String)，需要转换为Long
                if (principal instanceof String) {
                    teacherNo = Long.parseLong((String) principal);
                } else if (principal instanceof Long) {
                    // 兼容管理员登录（principal是userId）
                    teacherNo = (Long) principal;
                }
            } catch (NumberFormatException e) {
                log.error("无法解析当前登录教师工号: {}", e.getMessage());
                return Result.error("无法获取当前登录用户信息");
            }
            
            if (teacherNo == null) {
                return Result.error("无法获取当前登录用户信息");
            }
            
            log.info("当前登录教师工号: {}", teacherNo);
            Map<String, Object> result = courseScheduleService.importFromExcel(file, teacherNo);
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
    @GetMapping("/query")
    @Operation(summary = "分页查询课表", description = "分页查询课表，默认查询当前登录教师的课表。可通过teacherNo、className等参数进行过滤查询")
    public Result<Page<CourseSchedule>> query(CourseScheduleQueryDTO queryDTO) {
        // 如果没有指定教师工号，则使用当前登录教师的工号
        if (queryDTO.getTeacherNo() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                try {
                    Object principal = authentication.getPrincipal();
                    Long currentTeacherNo = null;
                    
                    // principal现在是teacherNo (String)，需要转换为Long
                    if (principal instanceof String) {
                        currentTeacherNo = Long.parseLong((String) principal);
                    } else if (principal instanceof Long) {
                        // 兼容管理员登录
                        currentTeacherNo = (Long) principal;
                    }
                    
                    if (currentTeacherNo != null) {
                        queryDTO.setTeacherNo(currentTeacherNo);
                        log.info("使用当前登录教师工号查询课表: {}", currentTeacherNo);
                    } else {
                        log.warn("无法解析当前登录教师工号，将查询所有课表");
                    }
                } catch (NumberFormatException e) {
                    log.warn("无法解析当前登录教师工号，将查询所有课表: {}", e.getMessage());
                }
            }
        }
        
        log.info("查询课表请求，参数：{}", queryDTO);
        Page<CourseSchedule> result = courseScheduleService.queryPage(queryDTO);
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
     * 创建课表
     */
    @PostMapping("/add")
    @Operation(summary = "创建课表", description = "教师创建新课表，教师工号从登录状态获取，ID自动生成")
    public Result<CourseSchedule> addCourseSchedule(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课表信息") @RequestBody CourseSchedule courseSchedule) {
        log.info("创建课表，课表信息：{}", courseSchedule);
        
        // 获取当前登录教师的工号
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherNo = null;
        try {
            Object principal = authentication.getPrincipal();
            // principal现在是teacherNo (String)，需要转换为Long
            if (principal instanceof String) {
                teacherNo = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                // 兼容管理员登录
                teacherNo = (Long) principal;
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 设置教师工号
        courseSchedule.setTeacherNo(teacherNo);
        
        // 验证必填字段
        if (courseSchedule.getCourseName() == null || courseSchedule.getCourseName().trim().isEmpty()) {
            return Result.error("课程名称不能为空");
        }
        if (courseSchedule.getClassName() == null || courseSchedule.getClassName().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }
        if (courseSchedule.getWeekday() == null || courseSchedule.getWeekday() < 1 || courseSchedule.getWeekday() > 7) {
            return Result.error("星期几必须是1-7之间的数字");
        }
        if (courseSchedule.getStartTime() == null) {
            return Result.error("开始时间不能为空");
        }
        if (courseSchedule.getEndTime() == null) {
            return Result.error("结束时间不能为空");
        }
        if (courseSchedule.getClassroom() == null || courseSchedule.getClassroom().trim().isEmpty()) {
            return Result.error("教室不能为空");
        }
        if (courseSchedule.getSemester() == null || courseSchedule.getSemester().trim().isEmpty()) {
            return Result.error("学期不能为空");
        }
        if (courseSchedule.getSchoolYear() == null || courseSchedule.getSchoolYear().trim().isEmpty()) {
            return Result.error("学年不能为空");
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
        log.info("更新课表，课程名称：{}，班级名称：{}，课表信息：{}", 
                courseSchedule.getCourseName(), courseSchedule.getClassName(), courseSchedule);
        
        if (courseSchedule.getCourseName() == null || courseSchedule.getCourseName().trim().isEmpty()) {
            return Result.error("课程名称不能为空");
        }
        if (courseSchedule.getClassName() == null || courseSchedule.getClassName().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }
        
        // 获取当前登录教师的工号
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherNo = null;
        try {
            Object principal = authentication.getPrincipal();
            // principal现在是teacherNo (String)，需要转换为Long
            if (principal instanceof String) {
                teacherNo = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                // 兼容管理员登录
                teacherNo = (Long) principal;
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 根据课程名称、班级名称和教师工号查询课表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseSchedule.getCourseName().trim())
                   .eq(CourseSchedule::getClassName, courseSchedule.getClassName().trim())
                   .eq(CourseSchedule::getTeacherNo, teacherNo);
        
        CourseSchedule existing = courseScheduleService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("课表不存在或无权限修改");
        }
        
        // 设置ID以便更新
        courseSchedule.setId(existing.getId());
        courseSchedule.setTeacherNo(teacherNo);
        
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherNo = null;
        try {
            Object principal = authentication.getPrincipal();
            // principal现在是teacherNo (String)，需要转换为Long
            if (principal instanceof String) {
                teacherNo = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                // 兼容管理员登录
                teacherNo = (Long) principal;
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 根据课程名称、班级名称和教师工号查询课表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseName.trim())
                   .eq(CourseSchedule::getClassName, className.trim())
                   .eq(CourseSchedule::getTeacherNo, teacherNo);
        
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherNo = null;
        try {
            Object principal = authentication.getPrincipal();
            // principal现在是teacherNo (String)，需要转换为Long
            if (principal instanceof String) {
                teacherNo = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                // 兼容管理员登录
                teacherNo = (Long) principal;
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 根据课程名称、班级名称和教师工号查询课表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseName.trim())
                   .eq(CourseSchedule::getClassName, className.trim())
                   .eq(CourseSchedule::getTeacherNo, teacherNo);
        
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
    public Result<CourseSchedule> getCourseScheduleById(@Parameter(description = "课表ID") @PathVariable Long id) {
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
    public Result<String> updateCourseScheduleById(@Parameter(description = "课表ID") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "课表信息") @RequestBody CourseSchedule courseSchedule) {
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
    public Result<String> deleteCourseScheduleById(@Parameter(description = "课表ID") @PathVariable Long id) {
        log.info("根据ID删除课表，课表ID：{}", id);
        
        CourseSchedule existing = courseScheduleService.getById(id);
        if (existing == null) {
            return Result.error("课表不存在");
        }
        
        courseScheduleService.removeById(id);
        log.info("删除课表完成");
        return Result.success("删除成功");
    }
}