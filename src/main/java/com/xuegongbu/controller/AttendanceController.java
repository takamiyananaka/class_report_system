package com.xuegongbu.controller;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.service.AttendanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/attendance")
@Api(tags = "考勤管理")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService ;
    /**
     * 查询课程考所有勤记录
     */
    @GetMapping("/queryAttendance")
    @ApiOperation(value = "查询课程所有考勤记录", notes = "查询课程所有考勤记录")
    public Result<List<Attendance>> queryAttendanceByCourseId(
            @RequestParam(value = "courseId", required = true) Long courseId) {
        log.info("查询课程考勤记录，课程ID：{}", courseId);
        List<Attendance> attendanceList = attendanceService.queryAllAttendanceByCourseId(courseId);
        log.info("查询课程考勤记录完成，结果：{}", attendanceList);
        return Result.success(attendanceList);
    }

    /**
     * 手动考勤
     */
    @PostMapping("/manualAttendance")
    @ApiOperation(value = "手动考勤", notes = "手动考勤")
    public Result<String> manualAttendance(
            @RequestParam(value = "courseId", required = true) Long courseId){
        log.info("手动考勤，课程ID：{}", courseId);
        attendanceService.manualAttendance(courseId);
        log.info("手动考勤完成");
        return Result.success("考勤记录生成完成");
    }

    /**
     * 查询当前考勤记录
     */
    @GetMapping("/queryCurrentAttendance")
    @ApiOperation(value = "查询当前考勤记录", notes = "查询当前考勤记录")
    public Result<Attendance> queryCurrentAttendance(@RequestParam(value = "courseId", required = true) Long courseId){
        log.info("查询当前考勤记录，课程ID：{}", courseId);
        Attendance attendance = attendanceService.queryCurrentAttendance(courseId);
        return Result.success(attendance);
    }
}
