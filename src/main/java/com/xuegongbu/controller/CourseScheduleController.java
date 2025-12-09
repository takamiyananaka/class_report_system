package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.service.CourseScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    @ApiOperation(value = "Excel导入课表", notes = "通过上传Excel文件批量导入课表数据。Excel格式要求：第一行为表头，列顺序为：课程名称、教师ID、班级名称、星期几(1-7)、开始时间(HH:mm)、结束时间(HH:mm)、教室、学期、学年")
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
}
