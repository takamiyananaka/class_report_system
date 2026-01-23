package com.xuegongbu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.exception.BusinessException;
import com.xuegongbu.domain.*;
import com.xuegongbu.domain.Class;
import com.xuegongbu.dto.*;
import com.xuegongbu.mapper.AttendanceMapper;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.service.*;
import com.xuegongbu.util.ClassTimeUtil;
import com.xuegongbu.util.CountUtil;
import com.xuegongbu.vo.AttendanceChartVO;
import com.xuegongbu.vo.AttendanceVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private AttendanceDailyReportService attendanceDailyReportService;
    @Autowired
    private AttendanceCourseReportService attendanceCourseReportService;
    @Autowired
    private SemesterService semesterService;


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

        //检查是否在上课时间内（根据节次和日期判断）
        //查询是否在日期范围内
        String semesterName = course.getSemesterName();
        Semester semester = semesterService.lambdaQuery().eq(Semester::getSemesterName, semesterName).one();
        List<LocalDate> dateList = ClassTimeUtil.getCourseDateRange(semester,course.getWeekRange());
        if(!(dateList.get(0).isBefore(LocalDate.now())&&LocalDate.now().isBefore(dateList.get(dateList.size()-1)))){
            log.error("不在该课程的日期范围内："+dateList);
            throw new BusinessException("不在该课程的日期范围内");
        }
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
        //由当前时间按照"HH:MM"格式生成checkTime
        LocalDateTime checkTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        //查找当前分种内当前课程考勤记录是否已存在，若存在则不再新生成考勤记录，同分种只有一条考勤记录
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attendance::getCourseId, courseId)
                .eq(Attendance::getCheckTime, checkTime);
        Attendance exixtingAttendance = getOne(queryWrapper);
        if (exixtingAttendance != null) {
            log.error("当前分钟内当前课程考勤记录已存在："+exixtingAttendance.getId());
            return exixtingAttendance;
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
        //调用考勤报表更新方法
       attendanceDailyReportService.updateReport(attendance,course);
       attendanceCourseReportService.updateReport(attendance,course);
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
    public Page<AttendanceVO> queryAttendanceReport(AttendanceReportQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        Page<AttendanceVO> voPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Attendance> attendanceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<CourseSchedule> courseScheduleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Class> classLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Object objRole = StpUtil.getSession().get("role");
        if (queryDTO.getCollegeNames()!=null&&!queryDTO.getCollegeNames().isEmpty()&&objRole.equals("admin")) {
            LambdaQueryWrapper<College> collegeLambdaQueryWrapper = new LambdaQueryWrapper<>();

            List<String> collegeNames = queryDTO.getCollegeNames();
            int flag = 0 ;
            for (String collegeName : collegeNames) {
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
        if (queryDTO.getTeacherNames()!=null&&!queryDTO.getTeacherNames().isEmpty()){
            List<String> teacherNames = queryDTO.getTeacherNames();
            LambdaQueryWrapper<Teacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
            int flag = 0 ;
            for (String teacherName : teacherNames) {
                if (!StringUtil.isBlank(teacherName)) {
                    if (flag == 0) {
                        flag = 1;
                        teacherLambdaQueryWrapper.like(Teacher::getRealName, teacherName.trim());
                    } else {
                        teacherLambdaQueryWrapper.or().like(Teacher::getRealName, teacherName.trim());
                    }
                }
            }
            List<Teacher> teachers = teacherService.list(teacherLambdaQueryWrapper);
            if (!teachers.isEmpty()) {
                classLambdaQueryWrapper.in(Class::getTeacherNo, teachers.stream().map(Teacher::getTeacherNo).toArray());
            }else {
                return new Page<>();
            }
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

        if (queryDTO.getClassNames()!=null&&!queryDTO.getClassNames().isEmpty()){
            List<String> classNames = queryDTO.getClassNames();
            int flag = 0 ;
            for (String className : classNames) {
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
        if(queryDTO.getCourseTeachers()!=null&&!queryDTO.getCourseTeachers().isEmpty()){
            List<String> courseTeachers = queryDTO.getCourseTeachers();
            int flag = 0 ;
            for (String courseTeacher : courseTeachers) {
                if (!StringUtil.isBlank(courseTeacher)) {
                    if (flag == 0) {
                        flag = 1;
                        courseScheduleLambdaQueryWrapper.like(CourseSchedule::getTeacherName, courseTeacher.trim());
                    } else {
                        courseScheduleLambdaQueryWrapper.or().like(CourseSchedule::getTeacherName, courseTeacher.trim());
                    }
                }
            }
        }

        if (queryDTO.getOrderNos()!=null&&!queryDTO.getOrderNos().isEmpty()){
            List<String> orderNos = queryDTO.getOrderNos();
            int flag = 0 ;
            for (String orderNo : orderNos) {
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
        if (queryDTO.getCourseTypes()!=null&&!queryDTO.getCourseTypes().isEmpty()){
            courseScheduleLambdaQueryWrapper.in(CourseSchedule::getCourseType, queryDTO.getCourseTypes());
        }
        if (queryDTO.getSemester()!=null&&!queryDTO.getSemester().isEmpty()){
            courseScheduleLambdaQueryWrapper.in(CourseSchedule::getSemesterName, queryDTO.getSemester());
        }
        List<CourseSchedule> courseSchedules = courseScheduleMapper.selectList(courseScheduleLambdaQueryWrapper);
        if (!courseSchedules.isEmpty()){
            attendanceLambdaQueryWrapper.in(Attendance::getCourseId, courseSchedules.stream().map(CourseSchedule::getId).toArray());
        }else {
            return new Page<>();
        }

        if (queryDTO.getStartDate() != null && queryDTO.getEndDate() != null){
            attendanceLambdaQueryWrapper.le(Attendance::getCheckTime, queryDTO.getStartDate().atStartOfDay())
                    .ge(Attendance::getCheckTime, queryDTO.getEndDate().plusDays(1).atStartOfDay());
        }

        Page<Attendance>  page = this.page(new Page<>(pageNum, pageSize), attendanceLambdaQueryWrapper);

        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        List<Attendance> attendanceList = page.getRecords();
        List<AttendanceVO> voList = convertToVO(attendanceList);
        voPage.setRecords(voList);

        return voPage;
    }


    private List<AttendanceVO> convertToVO(List<Attendance> attendanceList) {
        if(attendanceList.isEmpty()){
            return Collections.emptyList();
        }
        List<AttendanceVO> voList = new ArrayList<>(attendanceList.size());

        for (Attendance attendance : attendanceList) {
            AttendanceVO vo = new AttendanceVO();
            vo.setId(attendance.getId());
            vo.setCourseId(attendance.getCourseId());
            vo.setCheckTime(attendance.getCheckTime());
            vo.setActualCount(attendance.getActualCount());
            vo.setExpectedCount(attendance.getExpectedCount());
            vo.setAttendanceRate(attendance.getAttendanceRate());
            vo.setImageUrl(attendance.getImageUrl());
            vo.setCheckType(attendance.getCheckType());
            vo.setStatus(attendance.getStatus());
            vo.setRemark(attendance.getRemark());
            voList.add(vo);
        }

        // 获取每一个考勤记录对应的course,通过一次数据库访问
        List<String> courseIds = voList.stream()
                .map(AttendanceVO::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询课程信息
        Map<String, List<Course>> courseMap;
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<Course> courseLambdaQueryWrapper = new LambdaQueryWrapper<>();
            courseLambdaQueryWrapper.in(Course::getCourseId, courseIds);
            List<Course> courses = courseMapper.selectList(courseLambdaQueryWrapper);
            courseMap = courses.stream()
                    .collect(Collectors.groupingBy(Course::getCourseId));
        } else {
            courseMap = new HashMap<>();
        }

        // 批量查询课程安排信息
        List<CourseSchedule> courseSchedules = courseScheduleService.listByIds(courseIds);
        Map<String, CourseSchedule> courseScheduleMap = courseSchedules.stream()
                .collect(Collectors.toMap(CourseSchedule::getId, courseSchedule -> courseSchedule));

        // 提取所有涉及的班级ID，一次性查询班级信息
        Set<String> allClassIds = new HashSet<>();
        for (Map.Entry<String, List<Course>> entry : courseMap.entrySet()) {
            List<Course> courseList = entry.getValue();
            for (Course course : courseList) {
                if (course.getClassId() != null) {
                    allClassIds.add(course.getClassId());
                }
            }
        }

        // 批量查询班级信息
        List<Class> allClasses = new ArrayList<>();
        if (!allClassIds.isEmpty()) {
            LambdaQueryWrapper<Class> classLambdaQueryWrapper = new LambdaQueryWrapper<>();
            classLambdaQueryWrapper.in(Class::getId, allClassIds);
            allClasses = classMapper.selectList(classLambdaQueryWrapper);
        }

        // 创建班级ID到班级名称的映射
        Map<String, String> classIdToNameMap = allClasses.stream()
                .collect(Collectors.toMap(Class::getId, Class::getClassName));

        // 填充VO对象
        voList.forEach(vo -> {
            List<Course> courseses = courseMap.get(vo.getCourseId());
            if (courseses != null && !courseses.isEmpty()) {
                // 根据课程列表获取对应的班级名称
                List<String> classNames = new ArrayList<>();
                for (Course course : courseses) {
                    String className = classIdToNameMap.get(course.getClassId());
                    if (className != null) {
                        classNames.add(className);
                    }
                }
                vo.setClassNames(classNames);
            }

            CourseSchedule courseSchedule = courseScheduleMap.get(vo.getCourseId());
            if (courseSchedule != null) {
                vo.setCourseName(courseSchedule.getCourseName());
                vo.setOrderNo(courseSchedule.getOrderNo());
                vo.setCourseNo(courseSchedule.getCourseNo());
                vo.setTeacherName(courseSchedule.getTeacherName());
                vo.setCourseType(courseSchedule.getCourseType());
                vo.setSemesterName(courseSchedule.getSemesterName());
            }
        });

        return voList;
    }


    @Override
    public void exportAttendanceReport(AttendanceReportQueryDTO queryDTO, HttpServletResponse response) {
        // 设置分页参数，一次性获取所有数据
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(Integer.MAX_VALUE);
        Page<AttendanceVO> attendancePage = queryAttendanceReport(queryDTO);
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        String fileName = "attendanceReport_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        // 使用try-with-resources确保ExcelWriter正确关闭
        try (com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            // Sheet 1: 元数据信息
            writeMetadataSheet(excelWriter, attendancePage, queryDTO);

            // Sheet 2: 任课老师考勤率汇总
            writeTeacherAttendanceSheet(excelWriter, attendancePage, queryDTO);

            // Sheet 3: 课程考勤率汇总
            writeCourseAttendanceSheet(excelWriter, attendancePage, queryDTO);

            // Sheet 4: 辅导员老师考勤率汇总
            //writeCounselorAttendanceSheet(excelWriter, attendancePage, queryDTO);

            // Sheet 5: 学院考勤率汇总
            //writeCollegeAttendanceSheet(excelWriter, attendancePage,queryDTO);

            // Sheet 6: 班级考勤率汇总
            //writeClassAttendanceSheet(excelWriter, attendancePage,queryDTO);
            
            // 显式调用finish()完成写入
            excelWriter.finish();
        } catch (IOException e) {
            log.error("导出Excel时发生IO异常", e);
            throw new BusinessException("导出Excel时发生IO异常: " + e.getMessage());
        } catch (Exception e) {
            log.error("导出多Sheet考勤报表失败", e);
            throw new BusinessException("导出多Sheet考勤报表失败: " + e.getMessage());
        }
    }



    @Override
    public List<AttendanceChartVO> queryAttendanceChartByClass(AttendanceChartWithClassDTO queryDTO) {
        if(queryDTO.getGranularity()==4&&(queryDTO.getSemesterName()==null||queryDTO.getSemesterName().isEmpty())){
            throw new BusinessException("请选择学期");
        }
        List<AttendanceChartVO> attendanceChartVOList;
        LambdaQueryWrapper<Class> classLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Teacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(queryDTO.getCollegeName()!= null&&!queryDTO.getCollegeName().isEmpty()){
            LambdaQueryWrapper<College> collegeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            collegeLambdaQueryWrapper.eq(College::getName, queryDTO.getCollegeName());
            College college = collegeService.getOne(collegeLambdaQueryWrapper);
            if(college != null){
                teacherLambdaQueryWrapper.eq(Teacher::getCollegeNo, college.getCollegeNo());
            }
        }
        if(queryDTO.getTeacherName()!= null&&!queryDTO.getTeacherName().isEmpty()){
            teacherLambdaQueryWrapper.eq(Teacher::getRealName, queryDTO.getTeacherName());
        }
        List<Teacher> teachers = teacherService.list(teacherLambdaQueryWrapper);
        if(teachers.isEmpty()){
            return List.of();
        }
        classLambdaQueryWrapper.in(Class::getTeacherNo, teachers.stream().map(Teacher::getTeacherNo).collect(Collectors.toList()));
        if(queryDTO.getClassName()!= null&&!queryDTO.getClassName().isEmpty()){
            classLambdaQueryWrapper.eq(Class::getClassName, queryDTO.getClassName());
        }
        List<Class> classes = classService.list(classLambdaQueryWrapper);
        if(classes.isEmpty()){
            return List.of();
        }
        List<String> classIds = classes.stream()
                .map(Class::getId)
                .collect(Collectors.toList());
        String semesterName = "";
        if(queryDTO.getSemesterName()!= null&&!queryDTO.getSemesterName().isEmpty()){
            semesterName = queryDTO.getSemesterName();
        }
       List<AttendanceDailyReport> attendanceDailyReports= attendanceDailyReportService.getAttendanceChartByClassIdAndType(classIds, queryDTO.getGranularity(),semesterName);
        // 将同一天的数据整合到一个点
        Map<LocalDate, List<AttendanceDailyReport>> groupedByDate = attendanceDailyReports.stream()
                .collect(Collectors.groupingBy(AttendanceDailyReport::getReportDate));
        
        attendanceChartVOList = groupedByDate.entrySet().stream()
                .map(entry -> {
                    AttendanceChartVO attendanceChartVO = new AttendanceChartVO();
                    attendanceChartVO.setDate(entry.getKey());
                    // 计算当天的平均考勤率
                    List<AttendanceDailyReport> reports = entry.getValue();
                    BigDecimal averageRate = reports.stream()
                            .map(AttendanceDailyReport::getAverageAttendanceRate)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(reports.size()), RoundingMode.HALF_UP);
                    attendanceChartVO.setAttendRate(averageRate);
                    return attendanceChartVO;
                })
                .sorted(Comparator.comparing(AttendanceChartVO::getDate)) // 按日期排序
                .collect(Collectors.toList());
        return attendanceChartVOList;
    }

    @Override
    public List<AttendanceChartVO> queryAttendanceChartByCourse(AttendanceChartWithCourseDTO queryDTO) {
        if(queryDTO.getGranularity()==4&&(queryDTO.getSemesterName()==null||queryDTO.getSemesterName().isEmpty())){
            throw new BusinessException("请选择学期");
        }
        List<AttendanceChartVO> attendanceChartVOList = new ArrayList<>();
        LambdaQueryWrapper<CourseSchedule> courseScheduleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<AttendanceCourseReport> attendanceCourseReportLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(queryDTO.getTeacherName()!= null&&!queryDTO.getTeacherName().isEmpty()){
            courseScheduleLambdaQueryWrapper.eq(CourseSchedule::getTeacherName, queryDTO.getTeacherName());
        }
        if(queryDTO.getSemesterName()!= null&&!queryDTO.getSemesterName().isEmpty()){
            courseScheduleLambdaQueryWrapper.eq(CourseSchedule::getSemesterName, queryDTO.getSemesterName());
        }
        if(queryDTO.getOrderNo()!= null&&!queryDTO.getOrderNo().isEmpty()){
            courseScheduleLambdaQueryWrapper.eq(CourseSchedule::getOrderNo, queryDTO.getOrderNo());
        }
        List<CourseSchedule> courseSchedules = courseScheduleMapper.selectList(courseScheduleLambdaQueryWrapper);
        if(courseSchedules.isEmpty()){
            return List.of();
        }
        List<String> courseOrderNos = courseSchedules.stream()
                .map(CourseSchedule::getOrderNo)
                .collect(Collectors.toList());
        List<AttendanceCourseReport> attendanceCourseReportList = attendanceCourseReportService.getReportsByOrderNoAndType(courseOrderNos,queryDTO.getGranularity());
        // 将同一天的数据整合到一个点
        Map<LocalDate, List<AttendanceCourseReport>> groupedByDate = attendanceCourseReportList.stream()
                .collect(Collectors.groupingBy(AttendanceCourseReport::getReportDate));
        
        attendanceChartVOList = groupedByDate.entrySet().stream()
                .map(entry -> {
                    AttendanceChartVO attendanceChartVO = new AttendanceChartVO();
                    attendanceChartVO.setDate(entry.getKey());
                    // 计算当天的平均考勤率
                    List<AttendanceCourseReport> reports = entry.getValue();
                    BigDecimal averageRate = reports.stream()
                            .map(AttendanceCourseReport::getAverageAttendanceRate)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(reports.size()), RoundingMode.HALF_UP);
                    attendanceChartVO.setAttendRate(averageRate);
                    return attendanceChartVO;
                })
                .sorted(Comparator.comparing(AttendanceChartVO::getDate)) // 按日期排序
                .collect(Collectors.toList());
        if(attendanceChartVOList.isEmpty()){
            return List.of();
        }

        return attendanceChartVOList;
    }
    private void calculateDailyAttendanceRate(AttendanceSummaryExcelDTO summary, List<AttendanceVO> attendanceList) {
        if (attendanceList != null && !attendanceList.isEmpty()) {
            summary.setOverallAttendanceRate(attendanceList.stream()
                    .map(AttendanceVO::getAttendanceRate)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(attendanceList.size()), RoundingMode.HALF_UP));
        }
    }

    private void calculateMonthlyAttendanceRate(AttendanceSummaryExcelDTO summary, List<AttendanceVO> attendanceList, AttendanceReportQueryDTO queryDTO) {
        if (attendanceList != null && !attendanceList.isEmpty()
                && queryDTO.getEndDate() != null && queryDTO.getStartDate() != null
                && queryDTO.getEndDate().isAfter(queryDTO.getStartDate().plusMonths(1))||queryDTO.getEndDate().equals(queryDTO.getStartDate().plusMonths(1))) {

            Map<LocalDateTime, List<AttendanceVO>> groupedByMonth = attendanceList.stream()
                    .collect(Collectors.groupingBy(attendanceVO ->
                            attendanceVO.getCheckTime().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                    ));

            Map<String, BigDecimal> monthlyAttendanceRateMap = groupedByMonth.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                            entry -> {
                                List<AttendanceVO> monthAttendanceList = entry.getValue();
                                return monthAttendanceList.stream()
                                        .map(AttendanceVO::getAttendanceRate)
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(monthAttendanceList.size()), RoundingMode.HALF_UP);
                            }
                    ));
            
            // 将Map转换为指定格式的字符串 "yyyy-mm:rate,yyyy-mm:rate,..."
            String monthlyAttendanceRateStr = monthlyAttendanceRateMap.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining(","));
            summary.setMonthlyAttendanceRate(monthlyAttendanceRateStr);
        } else {
            summary.setMonthlyAttendanceRate("");
        }
    }

    private void calculateSemesterAttendanceRate(AttendanceSummaryExcelDTO summary, List<AttendanceVO> attendanceList, AttendanceReportQueryDTO queryDTO) {
        if (attendanceList != null && !attendanceList.isEmpty()
                && queryDTO.getStartDate() != null && queryDTO.getSemester() != null && !queryDTO.getSemester().isEmpty()) {

            Map<String, List<AttendanceVO>> groupedBySemester = attendanceList.stream()
                    .collect(Collectors.groupingBy(AttendanceVO::getSemesterName));

            Map<String, BigDecimal> semesterAttendanceRateMap = groupedBySemester.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                List<AttendanceVO> semesterAttendanceList = entry.getValue();
                                return semesterAttendanceList.stream()
                                        .map(AttendanceVO::getAttendanceRate)
                                        .filter(Objects::nonNull)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(semesterAttendanceList.size()), RoundingMode.HALF_UP);
                            }
                    ));
            
            // 将Map转换为指定格式的字符串 "semester:rate,semester:rate,..."
            String semesterAttendanceRateStr = semesterAttendanceRateMap.entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining(","));
            summary.setSemesterAttendanceRate(semesterAttendanceRateStr);
        } else {
            summary.setSemesterAttendanceRate("");
        }
    }


    private void writeMetadataSheet(com.alibaba.excel.ExcelWriter excelWriter,Page<AttendanceVO> attendancePage,AttendanceReportQueryDTO queryDTO) {
        try {
            // 准备元数据信息
            List<List<String>> head = new ArrayList<>();
            head.add(Arrays.asList("筛选条件", "值"));

            List<List<Object>> data = new ArrayList<>();


            WriteSheet sheet = EasyExcel.writerSheet("元数据").head(head).build();
            excelWriter.write(data, sheet);
        } catch (Exception e) {
            log.error("写入元数据Sheet失败", e);
            throw new BusinessException("写入元数据Sheet失败");
        }
    }
    
    private void writeTeacherAttendanceSheet(ExcelWriter excelWriter, Page<AttendanceVO> attendancePage, AttendanceReportQueryDTO queryDTO) {
        // 获取符合条件的考勤记录，按任课老师分组统计
        List<AttendanceSummaryExcelDTO> summaryList = new ArrayList<>();

        if (attendancePage.getRecords() != null && !attendancePage.getRecords().isEmpty()) {
            //按任课老师分组
           Map<String,List<AttendanceVO>> groupedByTeacher = attendancePage.getRecords().stream()
                .collect(Collectors.groupingBy(AttendanceVO::getTeacherName));
            
            for (Map.Entry<String, List<AttendanceVO>> entry : groupedByTeacher.entrySet()) {
                String teacherName = entry.getKey();
                List<AttendanceVO> attendanceList = entry.getValue();
                AttendanceSummaryExcelDTO summary = new AttendanceSummaryExcelDTO();
                summary.setName(teacherName);
                calculateDailyAttendanceRate(summary, attendanceList);
                calculateMonthlyAttendanceRate(summary, attendanceList, queryDTO);
                calculateSemesterAttendanceRate(summary, attendanceList, queryDTO);
                //统计时间范围
                summary.setStatisticsTime(queryDTO.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "至" + queryDTO.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                summary.setIdentifier(teacherName);
                summaryList.add(summary);
                }
            }

        
        WriteSheet sheet = EasyExcel.writerSheet("任课老师考勤率").head(AttendanceSummaryExcelDTO.class).build();
        excelWriter.write(summaryList, sheet);
    }
    
    private void writeCourseAttendanceSheet(ExcelWriter excelWriter, Page<AttendanceVO> attendancePage, AttendanceReportQueryDTO queryDTO) {
        try {
            // 获取符合条件的考勤记录，按课程分组统计
            List<AttendanceSummaryExcelDTO> summaryList = new ArrayList<>();
            
            if (attendancePage.getRecords() != null && !attendancePage.getRecords().isEmpty()) {
                //按课序号分组
              Map<String,List<AttendanceVO>> groupedByCourse = attendancePage.getRecords().stream()
                .collect(Collectors.groupingBy(AttendanceVO::getCourseNo));

              for (Map.Entry<String, List<AttendanceVO>> entry : groupedByCourse.entrySet()) {
                  String courseNo = entry.getKey();
                  List<AttendanceVO> attendanceList = entry.getValue();
                  AttendanceSummaryExcelDTO summary = new AttendanceSummaryExcelDTO();
                  summary.setIdentifier(courseNo);
                  summary.setName(attendanceList.get(0).getCourseName());
                  calculateDailyAttendanceRate(summary, attendanceList);
                  calculateMonthlyAttendanceRate(summary, attendanceList, queryDTO);
                  calculateSemesterAttendanceRate(summary, attendanceList, queryDTO);
                  summary.setStatisticsTime(queryDTO.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "至" + queryDTO.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                  summaryList.add(summary);
              }

            }
            
            WriteSheet sheet = EasyExcel.writerSheet("课程考勤率").head(AttendanceSummaryExcelDTO.class).build();
            excelWriter.write(summaryList, sheet);
        } catch (Exception e) {
            log.error("写入课程考勤率Sheet失败", e);
            throw new BusinessException("写入课程考勤率Sheet失败");
        }
    }
    
    private void writeCounselorAttendanceSheet(ExcelWriter excelWriter, Page<AttendanceVO> attendancePage, AttendanceReportQueryDTO queryDTO) {
        try {
            //按班级分组统计（通过班级关联到辅导员）
            List<AttendanceSummaryExcelDTO> summaryList = new ArrayList<>();
            if (attendancePage.getRecords() != null && !attendancePage.getRecords().isEmpty()) {
                // 获取所有涉及的课程ID
                Set<String> courseIds = attendancePage.getRecords().stream()
                    .map(Attendance::getCourseId)
                    .collect(Collectors.toSet());
                
                // 批量查询课程安排信息
                List<CourseSchedule> courseSchedules = new ArrayList<>();
                if (!courseIds.isEmpty()) {
                    courseSchedules = courseScheduleMapper.selectList(
                        new LambdaQueryWrapper<CourseSchedule>()
                            .in(CourseSchedule::getId, courseIds)
                    );
                }
                
                // 将课程安排信息放入Map中便于快速查找
                Map<String, CourseSchedule> courseScheduleMap = courseSchedules.stream()
                    .collect(Collectors.toMap(CourseSchedule::getId, cs -> cs));
                
                // 获取所有涉及的班级ID
                Set<String> classIds = new HashSet<>();
                for (Attendance attendance : attendancePage.getRecords()) {
                    String courseId = attendance.getCourseId();
                    CourseSchedule courseSchedule = courseScheduleMap.get(courseId);
                    if (courseSchedule != null) {
                        // 假设课程与班级有关联
                        List<String> classIdsFromCourse = courseMapper.selectClassIdsByCourseId(courseId);
                        classIds.addAll(classIdsFromCourse);
                    }
                }
                
                // 批量查询班级信息
                List<Class> classList = classMapper.selectList(
                    new LambdaQueryWrapper<Class>()
                        .in(Class::getId, classIds)
                );
                
                // 将班级信息放入Map中便于快速查找
                Map<String, Class> classMap = classList.stream()
                    .collect(Collectors.toMap(Class::getId, c -> c));
                
                // 对每个班级计算平均考勤率
                for (String classId : classIds) {
                    Class clazz = classMap.get(classId);
                    if (clazz != null) {
                        // 查找该班级相关的考勤记录并计算平均考勤率
                        double avgRate = attendancePage.getRecords().stream()
                            .filter(attendance -> {
                                String courseId = attendance.getCourseId();
                                CourseSchedule courseSchedule = courseScheduleMap.get(courseId);
                                if (courseSchedule != null) {
                                    List<String> classIdsFromCourse = courseMapper.selectClassIdsByCourseId(courseId);
                                    return classIdsFromCourse.contains(classId);
                                }
                                return false;
                            })
                            .mapToDouble(attendance -> attendance.getAttendanceRate().doubleValue())
                            .average()
                            .orElse(0.0);
                        
                        AttendanceSummaryExcelDTO summary = new AttendanceSummaryExcelDTO();
                        summary.setIdentifier(clazz.getId());
                        summary.setName(clazz.getClassName());
                        summary.setOverallAttendanceRate(BigDecimal.valueOf(avgRate));
                        summaryList.add(summary);
                    }
                }
            }
            
            WriteSheet sheet = EasyExcel.writerSheet("辅导员考勤率").head(AttendanceSummaryExcelDTO.class).build();
            excelWriter.write(summaryList, sheet);
        } catch (Exception e) {
            log.error("写入辅导员考勤率Sheet失败", e);
            throw new RuntimeException("写入辅导员考勤率Sheet失败");
        }
    }
    
    private void writeCollegeAttendanceSheet(ExcelWriter excelWriter, Page<AttendanceVO> attendancePage, AttendanceReportQueryDTO queryDTO) {
        try {
            // 按学院分组统计
            List<AttendanceSummaryExcelDTO> summaryList = new ArrayList<>();
            if (attendancePage.getRecords() != null && !attendancePage.getRecords().isEmpty()) {
                // 获取所有涉及的课程ID
                Set<String> courseIds = attendancePage.getRecords().stream()
                    .map(Attendance::getCourseId)
                    .collect(Collectors.toSet());
                
                // 批量查询课程安排信息
                List<CourseSchedule> courseSchedules = new ArrayList<>();
                if (!courseIds.isEmpty()) {
                    courseSchedules = courseScheduleMapper.selectList(
                        new LambdaQueryWrapper<CourseSchedule>()
                            .in(CourseSchedule::getId, courseIds)
                    );
                }
                
                // 将课程安排信息放入Map中便于快速查找
                Map<String, CourseSchedule> courseScheduleMap = courseSchedules.stream()
                    .collect(Collectors.toMap(CourseSchedule::getId, cs -> cs));
                
                // 按学院分组统计考勤率
                Map<String, List<Attendance>> groupedByCollege = new HashMap<>();
                
                for (Attendance attendance : attendancePage.getRecords()) {
                    String courseId = attendance.getCourseId();
                    CourseSchedule courseSchedule = courseScheduleMap.get(courseId);
                    if (courseSchedule != null) {
                        // 获取课程所属的班级，进而获取学院信息
                        List<String> classIds = courseMapper.selectClassIdsByCourseId(courseId);
                        
                        // 批量查询班级信息
                        List<Class> classList = classMapper.selectList(
                            new LambdaQueryWrapper<Class>()
                                .in(Class::getId, classIds)
                        );
                        
                        // 批量查询学院信息
                        Set<String> collegeNos = classList.stream()
                            .map(Class::getTeacherNo)
                            .collect(Collectors.toSet());
                        
                        List<Teacher> teacherList = teacherService.list(
                            new LambdaQueryWrapper<Teacher>()
                                .in(Teacher::getTeacherNo, collegeNos)
                        );
                        
                        Map<String, Teacher> teacherMap = teacherList.stream()
                            .collect(Collectors.toMap(Teacher::getTeacherNo, t -> t));
                        
                        for (Class clazz : classList) {
                            Teacher teacher = teacherMap.get(clazz.getTeacherNo());
                            if (teacher != null) {
                                String collegeNo = teacher.getCollegeNo();
                                groupedByCollege.computeIfAbsent(collegeNo, k -> new ArrayList<>()).add(attendance);
                            }
                        }
                    }
                }
                
                // 批量查询学院信息
                Set<String> collegeNos = groupedByCollege.keySet();
                List<College> collegeList = collegeService.list(
                    new LambdaQueryWrapper<College>()
                        .in(College::getCollegeNo, collegeNos)
                );
                
                Map<String, College> collegeMap = collegeList.stream()
                    .collect(Collectors.toMap(College::getCollegeNo, c -> c));
                
                // 计算每个学院的平均考勤率
                for (Map.Entry<String, List<Attendance>> entry : groupedByCollege.entrySet()) {
                    String collegeNo = entry.getKey();
                    List<Attendance> collegeAttendances = entry.getValue();
                    double avgRate = collegeAttendances.stream()
                        .filter(attendance -> attendance.getAttendanceRate() != null)
                        .mapToDouble(attendance -> attendance.getAttendanceRate().doubleValue())
                        .average()
                        .orElse(0.0);
                    
                    College college = collegeMap.get(collegeNo);
                    AttendanceSummaryExcelDTO summary = new AttendanceSummaryExcelDTO();
                    summary.setIdentifier(collegeNo);
                    summary.setName(college != null ? college.getName() : "未知学院");
                    summary.setOverallAttendanceRate(BigDecimal.valueOf(avgRate));
                    summaryList.add(summary);
                }
            }
            
            WriteSheet sheet = EasyExcel.writerSheet("学院考勤率").head(AttendanceSummaryExcelDTO.class).build();
            excelWriter.write(summaryList, sheet);
        } catch (Exception e) {
            log.error("写入学院考勤率Sheet失败", e);
            throw new RuntimeException("写入学院考勤率Sheet失败");

        }
    }
    
    private void writeClassAttendanceSheet(ExcelWriter excelWriter, Page<AttendanceVO> attendancePage, AttendanceReportQueryDTO queryDTO) {
        try {
            // 获取符合条件的考勤记录，按班级分组统计
            List<AttendanceSummaryExcelDTO> summaryList = new ArrayList<>();
            if (attendancePage.getRecords() != null && !attendancePage.getRecords().isEmpty()) {
                // 获取所有涉及的课程ID
                Set<String> courseIds = attendancePage.getRecords().stream()
                    .map(Attendance::getCourseId)
                    .collect(Collectors.toSet());
                
                // 批量查询课程安排信息
                List<CourseSchedule> courseSchedules = new ArrayList<>();
                if (!courseIds.isEmpty()) {
                    courseSchedules = courseScheduleMapper.selectList(
                        new LambdaQueryWrapper<CourseSchedule>()
                            .in(CourseSchedule::getId, courseIds)
                    );
                }
                
                // 将课程安排信息放入Map中便于快速查找
                Map<String, CourseSchedule> courseScheduleMap = courseSchedules.stream()
                    .collect(Collectors.toMap(CourseSchedule::getId, cs -> cs));
                
                // 按班级分组统计考勤率
                Map<String, List<Attendance>> groupedByClass = new HashMap<>();
                
                for (Attendance attendance : attendancePage.getRecords()) {
                    String courseId = attendance.getCourseId();
                    CourseSchedule courseSchedule = courseScheduleMap.get(courseId);
                    if (courseSchedule != null) {
                        // 获取课程所属的班级
                        List<String> classIds = courseMapper.selectClassIdsByCourseId(courseId);
                        for (String classId : classIds) {
                            groupedByClass.computeIfAbsent(classId, k -> new ArrayList<>()).add(attendance);
                        }
                    }
                }
                
                // 批量查询班级信息
                Set<String> classIds = groupedByClass.keySet();
                List<Class> classList = new ArrayList<>();
                if (!classIds.isEmpty()) {
                    classList = classMapper.selectList(
                        new LambdaQueryWrapper<Class>()
                            .in(Class::getId, classIds)
                    );
                }
                
                // 将班级信息放入Map中便于快速查找
                Map<String, Class> classMap = classList.stream()
                    .collect(Collectors.toMap(Class::getId, c -> c));
                
                // 计算每个班级的平均考勤率
                for (Map.Entry<String, List<Attendance>> entry : groupedByClass.entrySet()) {
                    String classId = entry.getKey();
                    List<Attendance> classAttendances = entry.getValue();
                    double avgRate = classAttendances.stream()
                        .filter(attendance -> attendance.getAttendanceRate() != null)
                        .mapToDouble(attendance -> attendance.getAttendanceRate().doubleValue())
                        .average()
                        .orElse(0.0);
                    
                    Class clazz = classMap.get(classId);
                    AttendanceSummaryExcelDTO summary = new AttendanceSummaryExcelDTO();
                    summary.setIdentifier(classId);
                    summary.setName(clazz != null ? clazz.getClassName() : "未知班级");
                    summary.setOverallAttendanceRate(BigDecimal.valueOf(avgRate));
                    summaryList.add(summary);
                }
            }
            
            WriteSheet sheet = EasyExcel.writerSheet("班级考勤率").head(AttendanceSummaryExcelDTO.class).build();
            excelWriter.write(summaryList, sheet);
        } catch (Exception e) {
            log.error("写入班级考勤率Sheet失败", e);
            throw new IllegalStateException("写入班级考勤率Sheet失败: " + e.getMessage(), e);
        }
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
            if ((!currentTime.isBefore(classStartTime)) && (!currentTime.isAfter(classEndTime))) {
                return classNumber;
            }
        }
        return null;
    }


}