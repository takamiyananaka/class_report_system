package com.xuegongbu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.service.CourseScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Api(tags = "课表管理")
public class CourseScheduleController {

    @Autowired
    private CourseScheduleService courseScheduleService;

    /**
     * Excel导入课表
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Excel导入课表", notes = "通过上传Excel文件批量导入课表数据。教师ID将根据当前登录用户自动填充。Excel格式要求：第一行为表头，列顺序为：课程名称、班级名称、星期几(1-7)、开始时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式)、结束时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式)、教室、学期、学年")
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
                // 处理不同类型的principal
                if (principal instanceof Long) {
                    teacherNo = (Long) principal;
                } else if (principal instanceof String) {
                    teacherNo = Long.parseLong((String) principal);
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
    @ApiOperation(value = "分页查询课表", notes = "分页查询课表，默认查询当前登录教师的课表。可通过teacherNo、className等参数进行过滤查询")
    public Result<Page<CourseSchedule>> query(CourseScheduleQueryDTO queryDTO) {
        // 如果没有指定教师工号，则使用当前登录教师的工号
        if (queryDTO.getTeacherNo() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                try {
                    Object principal = authentication.getPrincipal();
                    Long currentTeacherNo = null;
                    
                    // 处理不同类型的principal
                    if (principal instanceof Long) {
                        currentTeacherNo = (Long) principal;
                    } else if (principal instanceof String) {
                        currentTeacherNo = Long.parseLong((String) principal);
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
     * 根据班级名称分页查询课表
     */
    @GetMapping("/queryByClass")
    @ApiOperation(value = "根据班级名称查询课表", notes = "根据班级名称分页查询课表")
    public Result<Page<CourseSchedule>> queryByClassName(
            @RequestParam(value = "className", required = true) String className,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        log.info("根据班级名称查询课表，班级：{}, pageNum={}, pageSize={}", className, pageNum, pageSize);
        
        CourseScheduleQueryDTO queryDTO = new CourseScheduleQueryDTO();
        queryDTO.setClassName(className);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        Page<CourseSchedule> result = courseScheduleService.queryPage(queryDTO);
        log.info("查询课表完成，共{}条记录", result.getTotal());
        return Result.success(result);
    }

    /**
     * 下载课表导入模板
     */
    @GetMapping("/downloadTemplate")
    @ApiOperation(value = "下载课表导入模板", notes = "下载Excel格式的课表导入模板文件")
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
    @ApiOperation(value = "创建课表", notes = "教师创建新课表，教师工号从登录状态获取，ID自动生成")
    public Result<CourseSchedule> addCourseSchedule(@RequestBody CourseSchedule courseSchedule) {
        log.info("创建课表，课表信息：{}", courseSchedule);
        
        // 获取当前登录教师的ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherId = null;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                teacherId = (Long) principal;
            } else if (principal instanceof String) {
                teacherId = Long.parseLong((String) principal);
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师ID: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherId == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 将教师ID设置为teacherNo（这里假设使用ID作为teacherNo）
        // 注意：根据实际情况，可能需要先查询Teacher表获取真实的teacherNo
        courseSchedule.setTeacherNo(teacherId);
        
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
    @ApiOperation(value = "更新课表", notes = "教师更新课表信息，只能更新自己的课表")
    public Result<String> updateCourseSchedule(@RequestBody CourseSchedule courseSchedule) {
        log.info("更新课表，课表ID：{}，课表信息：{}", courseSchedule.getId(), courseSchedule);
        
        if (courseSchedule.getId() == null) {
            return Result.error("课表ID不能为空");
        }
        
        // 获取当前登录教师的ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherId = null;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                teacherId = (Long) principal;
            } else if (principal instanceof String) {
                teacherId = Long.parseLong((String) principal);
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师ID: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherId == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 验证课表是否属于当前教师
        CourseSchedule existing = courseScheduleService.getById(courseSchedule.getId());
        if (existing == null) {
            return Result.error("课表不存在");
        }
        if (!existing.getTeacherNo().equals(teacherId)) {
            return Result.error("无权限修改其他教师的课表");
        }
        
        courseScheduleService.updateById(courseSchedule);
        log.info("更新课表完成");
        return Result.success("更新成功");
    }

    /**
     * 删除课表
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除课表", notes = "教师删除课表，只能删除自己的课表")
    public Result<String> deleteCourseSchedule(@PathVariable Long id) {
        log.info("删除课表，课表ID：{}", id);
        
        // 获取当前登录教师的ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherId = null;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                teacherId = (Long) principal;
            } else if (principal instanceof String) {
                teacherId = Long.parseLong((String) principal);
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师ID: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherId == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        // 验证课表是否属于当前教师
        CourseSchedule existing = courseScheduleService.getById(id);
        if (existing == null) {
            return Result.error("课表不存在");
        }
        if (!existing.getTeacherNo().equals(teacherId)) {
            return Result.error("无权限删除其他教师的课表");
        }
        
        courseScheduleService.removeById(id);
        log.info("删除课表完成");
        return Result.success("删除成功");
    }

    /**
     * 查询课表详情
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "查询课表详情", notes = "根据课表ID查询课表详情，只能查询自己的课表")
    public Result<CourseSchedule> getCourseSchedule(@PathVariable Long id) {
        log.info("查询课表详情，课表ID：{}", id);
        
        // 获取当前登录教师的ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error("未登录或登录已过期，请重新登录");
        }
        
        Long teacherId = null;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                teacherId = (Long) principal;
            } else if (principal instanceof String) {
                teacherId = Long.parseLong((String) principal);
            }
        } catch (NumberFormatException e) {
            log.error("无法解析当前登录教师ID: {}", e.getMessage());
            return Result.error("无法获取当前登录用户信息");
        }
        
        if (teacherId == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        CourseSchedule courseSchedule = courseScheduleService.getById(id);
        if (courseSchedule == null) {
            return Result.error("课表不存在");
        }
        
        // 验证课表是否属于当前教师
        if (!courseSchedule.getTeacherNo().equals(teacherId)) {
            return Result.error("无权限查看其他教师的课表");
        }
        
        log.info("查询课表详情完成");
        return Result.success(courseSchedule);
    }
}
