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
    @ApiOperation(value = "Excel导入课表", notes = "通过上传Excel文件批量导入课表数据。Excel格式要求：第一行为表头，列顺序为：课程名称、教师ID、班级名称、星期几(1-7)、开始时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式)、结束时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式)、教室、学期、学年")
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
    @GetMapping("/query")
    @ApiOperation(value = "分页查询课表", notes = "分页查询课表，默认查询当前登录教师的课表。可通过teacherId、className等参数进行过滤查询")
    public Result<Page<CourseSchedule>> query(CourseScheduleQueryDTO queryDTO) {
        // 如果没有指定教师ID，则使用当前登录教师的ID
        if (queryDTO.getTeacherId() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                try {
                    Object principal = authentication.getPrincipal();
                    Long currentTeacherId = null;
                    
                    // 处理不同类型的principal
                    if (principal instanceof Long) {
                        currentTeacherId = (Long) principal;
                    } else if (principal instanceof String) {
                        currentTeacherId = Long.parseLong((String) principal);
                    }
                    
                    if (currentTeacherId != null) {
                        queryDTO.setTeacherId(currentTeacherId);
                        log.info("使用当前登录教师ID查询课表: {}", currentTeacherId);
                    } else {
                        log.warn("无法解析当前登录教师ID，将查询所有课表");
                    }
                } catch (NumberFormatException e) {
                    log.warn("无法解析当前登录教师ID，将查询所有课表: {}", e.getMessage());
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
}
