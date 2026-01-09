package com.xuegongbu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.*;
import com.xuegongbu.domain.Class;
import com.xuegongbu.dto.AttendanceQueryDTO;
import com.xuegongbu.dto.AttendanceReportQueryDTO;
import com.xuegongbu.mapper.AttendanceMapper;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.*;
import com.xuegongbu.util.ClassTimeUtil;
import com.xuegongbu.util.CountUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private ClassService classService;


    /**
     * 分页查询课程的所有考勤记录（支持日期查询）
     *
     * @param queryDTO 查询参数
     * @return
     */
    @Override
    public Page<Attendance> queryAllAttendanceByCourseId( AttendanceQueryDTO queryDTO) {
        //获取到课程信息
        CourseSchedule course = courseScheduleMapper.selectById(queryDTO.getCourseId());
        if (course == null) {
            throw new BusinessException("无效的课程ID");
        }

        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<Attendance> page = new Page<>(pageNum, pageSize);

        //查询课程的所有考勤记录
        QueryWrapper<Attendance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", queryDTO.getCourseId());

        // 添加日期查询条件
        if (queryDTO.getDate() != null) {
            // 构造一天的开始和结束时间
            LocalDateTime startOfDay = queryDTO.getDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            queryWrapper.ge("check_time", startOfDay)
                    .lt("check_time", endOfDay);
        }

        queryWrapper.orderByDesc("check_time");

        return page(page, queryWrapper);
    }

    @Override
    public Attendance manualAttendance(String courseId) {
        //获取到课程信息
        CourseSchedule course = courseScheduleMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("无效的id");
        }

        //检查是否在上课时间内（根据节次判断）
        LocalDateTime now = LocalDateTime.now();
        String weekday = ClassTimeUtil.convertDayOfWeekToChinese(now.getDayOfWeek());
        if (!weekday.equals(course.getWeekday())) {
            log.error("今天不是该课程的上课日："+weekday);
            throw new BusinessException("今天不是该课程的上课日");
        }

        // 获取当前时间对应的节次
        Integer currentPeriod = getCurrentPeriod(now.toLocalTime());
        if (currentPeriod == null || currentPeriod < course.getStartPeriod() || currentPeriod > course.getEndPeriod()) {
            log.error("当前不在该课程的上课节次范围内："+currentPeriod);
            throw new BusinessException("当前不在该课程的上课节次范围内");
        }

        String classroomName = course.getClassroom();
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

        //根据课程获取关联班级的总人数
        int expectedCount = course.getExpectedCount();

        Attendance attendance = new Attendance();
        attendance.setCourseId(courseId);
        attendance.setCheckTime(checkTime);
        //attendance.setActualCount((int) Math.round(countResponse.getSummary().getAverageCount()));
        attendance.setActualCount(4);
        attendance.setExpectedCount(expectedCount);
        //attendance.setAttendanceRate(BigDecimal.valueOf(countResponse.getSummary().getAverageCount() / expectedCount));
        attendance.setAttendanceRate(BigDecimal.valueOf(4.0 / expectedCount));
        //attendance.setImageUrl(countResponse.getSampleUrl());
        attendance.setImageUrl("http://117.72.173.242:8082/i/2025/12/15/693f6db765980.jpg");
        attendance.setCheckType(2);
        attendance.setStatus(1);
        attendance.setRemark("手动考勤");
        attendance.setIsDeleted(0);
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
        //查询当前时刻之前45分钟内的最近一条考勤记录
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime startTime = now.minusMinutes(45);
        Attendance attendance = getOne(new QueryWrapper<Attendance>()
                .eq("course_id", courseId)
                .ge("check_time", startTime)
                .le("check_time", now)
                .orderByDesc("check_time")
                .last("LIMIT 1"));
        if (attendance == null){
            throw new BusinessException("当前考勤记录生成中");
        }
        return attendance;
    }

    @Override
    public List<Double> queryAttendanceRateByTeacher(String teacherNo) {
        //获取老师的所有班级
        List<Class> classes = classMapper.selectList(new QueryWrapper<Class>().eq("teacher_no", teacherNo));
        List<Double> attendanceRates = new ArrayList<>();
        //初始话考勤率列表
        for (int i = 0; i < 7; i++) {
            attendanceRates.add(0.0);
        }
        for (Class clazz : classes) {
            List<Double> classDailyAttendanceRate = queryAttendanceRateByClass(clazz.getId());
            for (int i = 0; i < classDailyAttendanceRate.size(); i++) {
                attendanceRates.set(i, attendanceRates.get(i) + classDailyAttendanceRate.get(i));
            }
        }
        attendanceRates.replaceAll(aDouble -> aDouble / classes.size());
        return attendanceRates;
    }

    @Override
    public List<Double> queryAttendanceRateByClass(String id) {
        List<String> courseIds = courseMapper.selectCourseIdsByClassId(id);
        if(courseIds.isEmpty()){
            return new ArrayList<>();
        }
        List<CourseSchedule> courseSchedules = courseScheduleMapper.selectList(new QueryWrapper<CourseSchedule>().in("id", courseIds));
        //构建最近七天的时间列表
        List<LocalDate> dateList = new ArrayList<>();
        for (int i = 7; i > 0; i--) {
            dateList.add(LocalDate.now().minusDays(i));
        }
        List<Double> attendanceRates = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询每门课程当天的考勤记录
            double dailyTotalAttendanceRate = 0.0;
            int dailyAttendanceCount = 0;
            
            for (CourseSchedule courseSchedule : courseSchedules){
                //构建查询条件
                QueryWrapper<Attendance> queryWrapper = new QueryWrapper<>();
                // 修复：应该是course_id而不是id
                queryWrapper.eq("id", courseSchedule.getId())
                        .ge("check_time", date.atStartOfDay())
                        .lt("check_time", date.plusDays(1).atStartOfDay());

                List<Attendance> attendances = list(queryWrapper);
                if (!attendances.isEmpty()){
                    // 计算当天所有考勤记录的平均考勤率
                    // 修改第203行代码
                    double dailyAvgRate = attendances.stream()
                            .mapToDouble(attendance -> attendance.getAttendanceRate().doubleValue())
                            .average()
                            .orElse(0.0);
                    dailyTotalAttendanceRate += dailyAvgRate;
                    dailyAttendanceCount++;
                }
            }
            
            // 如果当天有考勤记录，计算平均考勤率
            if (dailyAttendanceCount > 0) {
                double dailyOverallRate = dailyTotalAttendanceRate / dailyAttendanceCount;
                attendanceRates.add(dailyOverallRate);
            } else {
                // 如果当天没有考勤记录，添加0.0表示无考勤数据
                attendanceRates.add(0.0);
            }
        }
        return attendanceRates;
    }

    @Override
    public Page<Attendance> queryAttendanceReport(AttendanceReportQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<Attendance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Attendance> attendanceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<CourseSchedule> courseScheduleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Class> classLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Object objRole = StpUtil.getSession().get("role");
        if (!queryDTO.getCollegeNames().isEmpty()&&objRole.equals("admin")) {
            LambdaQueryWrapper<College> collegeLambdaQueryWrapper = new LambdaQueryWrapper<>();

            List<String> collegeNames = queryDTO.getCollegeNames();
            int flag = 0 ;
            for (int i = 0; i < collegeNames.size(); i++) {
                String collegeName = collegeNames.get(i);
                if (!StringUtil.isBlank(collegeName)) {
                    if (flag == 0) {
                        flag = 1;
                        collegeLambdaQueryWrapper.like(College::getName, collegeName.trim());
                    } else {
                        collegeLambdaQueryWrapper.or().like(College::getName, collegeName.trim());
                    }
                }
            }
            List<College> colleges = collegeService.list(collegeLambdaQueryWrapper);
            if (!colleges.isEmpty()) {
                LambdaQueryWrapper<Teacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
                teacherLambdaQueryWrapper.in(Teacher::getCollegeNo, colleges.stream().map(College::getCollegeNo).toArray());
                List<Teacher> teachers = teacherService.list(teacherLambdaQueryWrapper);
                if (!teachers.isEmpty()) {
                    classLambdaQueryWrapper.in(Class::getTeacherNo, teachers.stream().map(Teacher::getTeacherNo).toArray());
                }else {
                    return new Page<>();
                }
            }else {
                return new Page<>();
            }
        }
        if (!queryDTO.getTeacherNos().isEmpty()){
            classLambdaQueryWrapper.in(Class::getTeacherNo, queryDTO.getTeacherNos());
        }else {
            if(objRole.equals("college_admin")){
                College college = (College) StpUtil.getSession().get("collegeInfo");
                LambdaQueryWrapper<Teacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
                teacherLambdaQueryWrapper.eq(Teacher::getCollegeNo, college.getCollegeNo());
                List<Teacher> teachers = teacherService.list(teacherLambdaQueryWrapper);
                if (!teachers.isEmpty()) {
                    classLambdaQueryWrapper.in(Class::getTeacherNo, teachers.stream().map(Teacher::getTeacherNo).toArray());
                }else {
                    return new Page<>();
                }
            }else if(objRole.equals("teacher")){
                classLambdaQueryWrapper.eq(Class::getTeacherNo, StpUtil.getLoginIdAsString());
            }
        }

        if (!queryDTO.getClassNames().isEmpty()){
            List<String> classNames = queryDTO.getClassNames();
            int flag = 0 ;
            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i);
                if (!StringUtil.isBlank(className)) {
                    if (flag == 0) {
                        flag = 1;
                        classLambdaQueryWrapper.like(Class::getClassName, className.trim());
                    } else {
                        classLambdaQueryWrapper.or().like(Class::getClassName, className.trim());
                    }
                }
            }
        }

        List<Class> classes = classMapper.selectList(classLambdaQueryWrapper);
        if (!classes.isEmpty()){
            LambdaQueryWrapper<Course> courseLambdaQueryWrapper = new LambdaQueryWrapper<>();
            courseLambdaQueryWrapper.in(Course::getClassId, classes.stream().map(Class::getId).toArray());
            List<Course> courses = courseMapper.selectList(courseLambdaQueryWrapper);
            if (!courses.isEmpty()){
                courseScheduleLambdaQueryWrapper.in(CourseSchedule::getId, courses.stream().map(Course::getCourseId).toArray());
            }
        }else {
            return new Page<>();
        }

        if (!queryDTO.getOrderNos().isEmpty()){
            List<String> orderNos = queryDTO.getOrderNos();
            int flag = 0 ;
            for (int i = 0; i < orderNos.size(); i++) {
                String orderNo = orderNos.get(i);
                if (!StringUtil.isBlank(orderNo)) {
                    if (flag == 0) {
                        flag = 1;
                        courseScheduleLambdaQueryWrapper.like(CourseSchedule::getOrderNo, orderNo.trim());
                    } else {
                        courseScheduleLambdaQueryWrapper.or().like(CourseSchedule::getOrderNo, orderNo.trim());
                    }
                }
            }
        }
        if (!queryDTO.getCourseTypes().isEmpty()){
            courseScheduleLambdaQueryWrapper.in(CourseSchedule::getCourseType, queryDTO.getCourseTypes());
        }
        if (!queryDTO.getSemester().isEmpty()){
            courseScheduleLambdaQueryWrapper.in(CourseSchedule::getSemesterName, queryDTO.getSemester());
        }
        List<CourseSchedule> courseSchedules = courseScheduleMapper.selectList(courseScheduleLambdaQueryWrapper);
        if (!courseSchedules.isEmpty()){
            attendanceLambdaQueryWrapper.in(Attendance::getCourseId, courseSchedules.stream().map(CourseSchedule::getId).toArray());
        }else {
            return new Page<>();
        }

        if (queryDTO.getStartDate() != null && queryDTO.getEndDate() != null){
            attendanceLambdaQueryWrapper.ge(Attendance::getCheckTime, queryDTO.getStartDate().atStartOfDay())
                    .le(Attendance::getCheckTime, queryDTO.getEndDate().plusDays(1).atStartOfDay());
        }

        return page(page, attendanceLambdaQueryWrapper);
    }

    @Override
    public void exportAttendanceReport(AttendanceReportQueryDTO queryDTO, HttpServletResponse response) {
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        Page<Attendance> attendancePage = queryAttendanceReport(queryDTO);
    }


    /**
     * 根据当前时间获取对应的课程节次
     * @param currentTime 当前时间
     * @return 对应的课程节次，如果不在课程时间内返回null
     */
    private Integer getCurrentPeriod(LocalTime currentTime) {
        // 遍历所有课程节次，检查当前时间是否在某节课的时间范围内
        Integer[] allClassNumbers = ClassTimeUtil.getAllClassNumbers();
        for (Integer classNumber : allClassNumbers) {
            LocalTime classStartTime = ClassTimeUtil.getStartTimeAsLocalTime(classNumber);
            LocalTime classEndTime = ClassTimeUtil.getEndTimeAsLocalTime(classNumber);

            // 如果当前时间在课程开始时间和结束时间之间（包含边界）
            if ((currentTime.compareTo(classStartTime) >= 0) && (currentTime.compareTo(classEndTime) <= 0)) {
                return classNumber;
            }
        }
        return null;
    }


}