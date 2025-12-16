package com.xuegongbu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.Attendance;
import com.xuegongbu.domain.Class;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CountResponse;
import com.xuegongbu.mapper.AttendanceMapper;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.*;
import com.xuegongbu.util.CountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private CountUtil countUtil;
    @Autowired
    private AlertService alertService;

    /**
     * 查询课程的所有考勤记录
     *
     * @param courseId
     * @return
     */
    @Override
    public List<Attendance> queryAllAttendanceByCourseId(String courseId) {
        //获取到课程信息
        CourseSchedule course = courseScheduleMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("无效的id");
        }
        //查询课程的所有考勤记录
        return list(new QueryWrapper<Attendance>().eq("course_id", courseId));
    }

    @Override
    public Attendance manualAttendance(String courseId) {
        //获取到课程信息
        CourseSchedule course = courseScheduleMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("无效的id");
        }
        //检查是否在上课时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.getDayOfWeek().getValue() != course.getWeekday()) {
            throw new BusinessException("今天不是该课程的上课日");
        }
        if (now.toLocalTime().isBefore(course.getStartTime()) || now.toLocalTime().isAfter(course.getEndTime())) {
            throw new BusinessException("不在上课时间");
        }
        String classroomName = course.getClassroom();
        String className = course.getClassName();
        //Map<String, String> deviceUrls = deviceService.getDeviceUrl(classroomName);
        Map<String, String> deviceUrls = deviceService.getDeviceUrl("成都校区/博学楼/博学楼A101");
        if (deviceUrls == null){
            throw new BusinessException("当前教室无可用的设备");
        }
        //调用模型
        //CountResponse countResponse = countUtil.getCount(deviceUrls);
        //生成考勤记录
        //由当前时间按照"HH:MM"格式生成checkTime
        LocalDateTime checkTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        //根据班级名字获取班级信息
        Class clazz = classMapper.selectOne(new QueryWrapper<Class>().eq("class_name",className));
        Attendance attendance = new Attendance();
        attendance.setCourseId(courseId);
        attendance.setCheckTime(checkTime);
        //attendance.setActualCount((int) Math.round(countResponse.getSummary().getAverageCount()));
        attendance.setActualCount(4);
        attendance.setExpectedCount(clazz.getCount());
        //attendance.setAttendanceRate(BigDecimal.valueOf(countResponse.getSummary().getAverageCount() / clazz.getCount()));
        attendance.setAttendanceRate(BigDecimal.valueOf(0.12));
        //attendance.setImageUrl(countResponse.getSampleUrl());
        attendance.setImageUrl("http://117.72.173.242:8082/i/2025/12/15/693f6db765980.jpg");
        attendance.setCheckType(2);
        attendance.setStatus(1);
        attendance.setRemark("手动考勤");

        save(attendance);
        
        // 检查是否需要生成预警记录
        alertService.checkAndGenerateAlert(attendance, course);

        return attendance;
    }

    @Override
    public Attendance queryCurrentAttendance(String courseId) {
        //获取到课程信息
        CourseSchedule course = courseScheduleMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("无效的id");
        }
        //查询当前时刻上下总共100分钟内的考勤记录，并选择最近的一条
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime startTime = now.minusMinutes(50);
        LocalDateTime endTime = now.plusMinutes(50);
        Attendance attendance = getOne(new QueryWrapper<Attendance>()
                .eq("course_id", courseId)
                .ge("check_time", startTime)
                .le("check_time", endTime)
                .orderByAsc("check_time")
                .last("LIMIT 1"));
        if (attendance == null){
            throw new BusinessException("当前考勤记录生成中");
        }
        return attendance;
    }
}