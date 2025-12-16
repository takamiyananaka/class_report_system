package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.service.AttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/attendance")
@Tag(name = "考勤管理", description = "考勤相关接口")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService ;
    /**
     * 查询课程考所有勤记录
     */
    @GetMapping("/queryAttendance")
    @Operation(summary = "查询课程所有考勤记录", description = "查询课程所有考勤记录")
    public Result<List<Attendance>> queryAttendanceByCourseId(
            @Parameter(description = "课程ID", required = true) @RequestParam(value = "courseId", required = true) String courseId) {
        log.info("查询课程考勤记录，课程ID：{}", courseId);
        List<Attendance> attendanceList = attendanceService.queryAllAttendanceByCourseId(courseId);
        log.info("查询课程考勤记录完成，结果：{}", attendanceList);
        return Result.success(attendanceList);
    }

    /**
     * 手动考勤
     */
    @GetMapping("/manualAttendance")
    @Operation(summary = "手动考勤", description = "手动考勤")
    public Result<String> manualAttendance(
            @Parameter(description = "课程ID", required = true) @RequestParam(value = "courseId", required = true) String courseId){
        log.info("手动考勤，课程ID：{}", courseId);
        attendanceService.manualAttendance(courseId);
        log.info("手动考勤完成");
        return Result.success("考勤记录生成完成");
    }

    /**
     * 查询当前考勤记录
     */
    @GetMapping("/queryCurrentAttendance")
    @Operation(summary = "查询当前考勤记录", description = "查询当前考勤记录")
    public Result<Attendance> queryCurrentAttendance(@Parameter(description = "课程ID", required = true) @RequestParam(value = "courseId", required = true) String courseId){
        log.info("查询当前考勤记录，课程ID：{}", courseId);
        Attendance attendance = attendanceService.queryCurrentAttendance(courseId);
        return Result.success(attendance);
    }

    /**
     * 根据ID查询考勤记录
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询考勤记录", description = "根据考勤ID查询考勤详情")
    public Result<Attendance> getAttendanceById(@Parameter(description = "考勤ID") @PathVariable String id) {
        log.info("根据ID查询考勤记录，考勤ID：{}", id);
        Attendance attendance = attendanceService.getById(id);
        if (attendance == null) {
            return Result.error("考勤记录不存在");
        }
        log.info("查询考勤记录完成");
        return Result.success(attendance);
    }

    /**
     * 创建考勤记录
     */
    @PostMapping("/add")
    @Operation(summary = "创建考勤记录", description = "创建新的考勤记录，ID自动生成")
    public Result<Attendance> addAttendance(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "考勤信息") @RequestBody Attendance attendance) {
        log.info("创建考勤记录，考勤信息：{}", attendance);
        
        // 验证必填字段
        if (attendance.getCourseId() == null) {
            return Result.error("课程ID不能为空");
        }
        
        attendanceService.save(attendance);
        log.info("创建考勤记录完成，考勤ID：{}", attendance.getId());
        return Result.success(attendance);
    }

    /**
     * 根据ID更新考勤记录
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "根据ID更新考勤记录", description = "通过考勤ID更新考勤信息")
    public Result<String> updateAttendanceById(@Parameter(description = "考勤ID") @PathVariable String id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "考勤信息") @RequestBody Attendance attendance) {
        log.info("根据ID更新考勤记录，考勤ID：{}，考勤信息：{}", id, attendance);
        
        Attendance existing = attendanceService.getById(id);
        if (existing == null) {
            return Result.error("考勤记录不存在");
        }
        
        // 设置ID以便更新
        attendance.setId(id);
        
        attendanceService.updateById(attendance);
        log.info("更新考勤记录完成");
        return Result.success("更新成功");
    }

    /**
     * 根据ID删除考勤记录
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "根据ID删除考勤记录", description = "通过考勤ID删除考勤记录")
    public Result<String> deleteAttendanceById(@Parameter(description = "考勤ID") @PathVariable String id) {
        log.info("根据ID删除考勤记录，考勤ID：{}", id);
        
        Attendance existing = attendanceService.getById(id);
        if (existing == null) {
            return Result.error("考勤记录不存在");
        }
        
        attendanceService.removeById(id);
        log.info("删除考勤记录完成");
        return Result.success("删除成功");
    }
}