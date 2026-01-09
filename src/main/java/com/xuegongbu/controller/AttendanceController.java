package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.dto.AttendanceQueryDTO;
import com.xuegongbu.dto.AttendanceReportQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xuegongbu.service.AttendanceService;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;
import com.xuegongbu.domain.Teacher;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/attendance")
@Tag(name = "考勤管理", description = "考勤相关接口")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService ;
    /**
     * 查询课程所有考勤记录（分页，支持按日期查询）
     */
    @PostMapping("/queryAttendance")
    @Operation(summary = "查询课程考勤记录列表", description = "查询课程所有考勤记录（分页，支持按日期查询）")
    @SaCheckRole(value = {"college_admin","admin","teacher"}, mode = SaMode.OR)
    public Result<Page<Attendance>> queryAttendanceByCourseId(@RequestBody AttendanceQueryDTO queryDTO) {
        log.info("查询课程考勤记录，参数：{}", queryDTO);
        
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() <= 0) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() <= 0) {
            queryDTO.setPageSize(10);
        }
        
        Page<Attendance> attendancePage = attendanceService.queryAllAttendanceByCourseId(queryDTO);
        log.info("查询课程考勤记录完成，共{}条记录", attendancePage.getTotal());
        return Result.success(attendancePage);
    }

    /**
     * 统计·报表查询考勤记录，维度：学院，班级（辅导员，班级名称），任课老师，课程类型，时间（学年，学期，月，周，日），
     */
    @PostMapping("/queryAttendanceReport")
    @Operation(summary = "统计·报表查询考勤记录", description = "统计·报表查询考勤记录，维度：学院，班级（辅导员，班级名称），课序号，课程类型，时间范围（具体日期范围），学期，三级角色统一用考勤报表页面查询")
    @SaCheckRole(value = {"college_admin","admin","teacher"}, mode = SaMode.OR)
    public Result<Page<Attendance>> queryAttendanceReport(@RequestBody AttendanceReportQueryDTO queryDTO){
        log.info("统计·报表查询考勤记录，参数：{}", queryDTO);
        Page<Attendance> attendancePage = attendanceService.queryAttendanceReport(queryDTO);
        log.info("统计·报表查询考勤记录完成，共{}条记录", attendancePage.getTotal());
        return Result.success(attendancePage);
    }

    /**
     * 报表生成接口（excel）
     */
    @PostMapping("/exportAttendanceReport")
    @Operation(summary = "报表生成接口（excel）", description = "报表生成接口（excel）,与查询共用查询条件，根据该查询条件对应的考勤记录生成报表")
    @SaCheckRole(value = {"college_admin","admin","teacher"}, mode = SaMode.OR)
    public Result<String> exportAttendanceReport(@RequestBody AttendanceReportQueryDTO queryDTO, HttpServletResponse response){
        log.info("报表生成接口（excel），参数：{}", queryDTO);
        attendanceService.exportAttendanceReport(queryDTO, response);
        log.info("报表生成接口（excel）完成");
        return Result.success("报表生成完成");
    }

    /**
     * 手动考勤
     */
    @GetMapping("/manualAttendance")
    @Operation(summary = "手动考勤", description = "手动考勤")
    @SaCheckRole(value = {"college_admin","admin","teacher"}, mode = SaMode.OR)
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
    @SaCheckRole(value = {"college_admin","admin","teacher"}, mode = SaMode.OR)
    public Result<Attendance> queryCurrentAttendance(@Parameter(description = "课程ID", required = true) @RequestParam(value = "courseId", required = true) String courseId){
        log.info("查询当前考勤记录，课程ID：{}", courseId);
        Attendance attendance = attendanceService.queryCurrentAttendance(courseId);
        return Result.success(attendance);
    }


    /**
     * 批量删除考勤记录
     */
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除考勤记录", description = "批量删除考勤记录")
    @SaCheckRole(value = {"admin"}, mode = SaMode.OR)
    public Result<String> deleteAttendanceBatch(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "考勤ID列表") @RequestBody List<String> ids) {
        log.info("批量删除考勤记录，考勤ID列表：{}", ids);

        attendanceService.removeByIds(ids);
        log.info("批量删除考勤记录完成");
        return Result.success("批量删除成功");
    }

    /**
     * 按老师查询最近一周的考勤率（考勤率数组，七天）
     */
    @GetMapping("/queryAttendanceRateByTeacher")
    @Operation(summary = "按老师查询其所有班级的最近一周的总平均考勤率", description = "按老师查询最近一周的考勤率，学院管理员和老师通用")
    @SaCheckRole(value = {"teacher", "college_admin"}, mode = SaMode.OR)
    public Result<List<Double>> queryAttendanceRateByTeacher(@Parameter(description = "老师工号,学院管理员查询时候需要") @RequestParam(value = "teacherNo", required = false) String teacherNo) {

        log.info("按老师查询其所有班级的最近一周的平均考勤率");
        if(StpUtil.hasRole("teacher")){
            teacherNo = getTeacherNoFromSession() ;
        }
        List<Double> attendanceRateList = attendanceService.queryAttendanceRateByTeacher(teacherNo);
        log.info("按老师查询其所有班级的最近一周的平均考勤率完成");
        return Result.success(attendanceRateList);
    }



    /**
     * 按班级查询最近一周的考勤率（考勤率数组，七天）
     */
    @GetMapping("/queryAttendanceRateByClass/{id}")
    @Operation(summary = "按班级查询最近一周的考勤率", description = "按班级查询最近一周的考勤率")
    @SaCheckRole("teacher")
    public Result<List<Double>> queryAttendanceRateByClass(@Parameter(description = "班级id") @PathVariable String id) {
        log.info("按班级查询最近一周的考勤率");
        List<Double> attendanceRateList = attendanceService.queryAttendanceRateByClass(id);
        log.info("按班级查询最近一周的考勤率完成");
        return Result.success(attendanceRateList);
    }

    /**
     * 获取老师工号
     */
    private String getTeacherNoFromSession() {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("用户未登录");
        }

        String teacherNo = null;
        try {
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof String) {
                teacherNo = (String) loginId;
            }
        } catch (Exception e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
            throw new RuntimeException("无法获取当前登录用户信息");
        }

        if (teacherNo == null) {
            throw new RuntimeException("无法获取当前登录用户信息");
        }

        return teacherNo;
    }
}