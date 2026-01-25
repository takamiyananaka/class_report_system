package com.xuegongbu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.*;
import com.xuegongbu.domain.Class;
import com.xuegongbu.dto.CourseScheduleExcelDTO;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.dto.CourseScheduleWithClassIdsDTO;
import com.xuegongbu.vo.CourseScheduleVO;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.mapper.SemesterMapper;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.service.*;
import com.xuegongbu.util.ClassTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class CourseScheduleServiceImpl extends ServiceImpl<CourseScheduleMapper, CourseSchedule> implements CourseScheduleService {
    
    private static final java.util.Set<String> VALID_WEEKDAYS = java.util.Set.of(
        "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"
    );
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private CourseService courseService;
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private CollegeAdminService collegeAdminService;

    @Autowired
    private CollegeService collegeService;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private SemesterMapper semesterMapper;
    @Autowired
    private SemesterService semesterService;

    @Override
    public CourseSchedule addCourseSchedule(CourseSchedule courseSchedule) {
        // 验证必填字段
        if (courseSchedule.getCourseName() == null || courseSchedule.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (courseSchedule.getWeekday() == null || courseSchedule.getWeekday().trim().isEmpty()) {
            throw new IllegalArgumentException("星期几不能为空");
        }
        if (!isValidWeekday(courseSchedule.getWeekday())) {
            throw new IllegalArgumentException("星期几格式不正确，应为：星期一、星期二、星期三、星期四、星期五、星期六、星期日");
        }
        if (courseSchedule.getWeekRange() == null || courseSchedule.getWeekRange().trim().isEmpty()) {
            throw new IllegalArgumentException("周次范围不能为空");
        }
        if (courseSchedule.getStartPeriod() == null || courseSchedule.getStartPeriod() < 1 || courseSchedule.getStartPeriod() > 12) {
            throw new IllegalArgumentException("开始节次必须是1-12之间的数字");
        }
        if (courseSchedule.getEndPeriod() == null || courseSchedule.getEndPeriod() < 1 || courseSchedule.getEndPeriod() > 12) {
            throw new IllegalArgumentException("结束节次必须是1-12之间的数字");
        }
        if (courseSchedule.getEndPeriod() < courseSchedule.getStartPeriod()) {
            throw new IllegalArgumentException("结束节次必须大于或等于开始节次");
        }
        if (courseSchedule.getClassroom() == null || courseSchedule.getClassroom().trim().isEmpty()) {
            throw new IllegalArgumentException("教室不能为空");
        }
        
        // ID会由MyBatis-Plus自动生成（雪花算法）
        this.save(courseSchedule);
        log.info("创建课表完成，课表ID：{}", courseSchedule.getId());
        return courseSchedule;
    }
    
    /**
     * 验证星期几格式是否正确
     * @param weekday 星期几（汉字格式）
     * @return 是否有效
     */
    private boolean isValidWeekday(String weekday) {
        return weekday != null && VALID_WEEKDAYS.contains(weekday.trim());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        

        // 检查文件扩展名和Content-Type
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("文件格式不正确，只支持.xlsx或.xls格式");
        }
        
        // 验证Content-Type
        if (contentType == null || (!contentType.contains("spreadsheetml") && !contentType.contains("excel") && !contentType.contains("ms-excel"))) {
            throw new IllegalArgumentException("文件类型不正确，只支持Excel文件");
        }
        
        try {
            // 读取Excel数据，按表头名称映射
            List<CourseScheduleExcelDTO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(CourseScheduleExcelDTO.class)
                    .sheet()
                    .headRowNumber(1)
                    .doReadSync();
            
            if (excelDataList == null || excelDataList.isEmpty()) {
                throw new IllegalArgumentException("Excel文件中没有数据");
            }
            
            log.info("从Excel读取到 {} 条数据", excelDataList.size());
            
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            // 逐行处理数据
            for (int i = 0; i < excelDataList.size(); i++) {
                int rowNum = i + 2; // Excel行号从2开始（第1行是表头）
                
                try {
                    CourseScheduleExcelDTO dto = excelDataList.get(i);
                    
                    // 验证必填字段是否完整
                    if (isBlank(dto.getCourseNo())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getCourseName())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getOrderNo())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getWeekRange())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getWeekday())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (!isValidWeekday(dto.getWeekday())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 验证并转换节次
                    Integer startPeriod = extractNumberFromString(dto.getStartPeriod());
                    Integer endPeriod = extractNumberFromString(dto.getEndPeriod());
                    
                    if (startPeriod == null || startPeriod < 1 || startPeriod > 12) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (endPeriod == null || endPeriod < 1 || endPeriod > 12) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    if (startPeriod > endPeriod) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    if (isBlank(dto.getClassroom())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 验证预到人数（必填）
                    Integer expectedCount = extractNumberFromString(dto.getExpectedCount());
                    if (expectedCount == null || expectedCount <= 0) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 创建课表对象
                    CourseSchedule courseSchedule = new CourseSchedule();
                    courseSchedule.setCourseNo(dto.getCourseNo().trim());
                    courseSchedule.setCourseName(dto.getCourseName().trim());
                    courseSchedule.setOrderNo(dto.getOrderNo().trim());
                    courseSchedule.setWeekRange(dto.getWeekRange().trim());
                    courseSchedule.setWeekday(dto.getWeekday().trim());
                    courseSchedule.setStartPeriod(startPeriod);
                    courseSchedule.setEndPeriod(endPeriod);
                    courseSchedule.setClassroom(dto.getClassroom().trim());
                    courseSchedule.setTeacherName(isBlank(dto.getTeacherName()) ? null : dto.getTeacherName().trim());
                    courseSchedule.setCourseType(isBlank(dto.getCourseType()) ? null : dto.getCourseType().trim());
                    // 使用上传的预到人数，不进行计算
                    courseSchedule.setExpectedCount(expectedCount);
                    

                    // 处理新增字段：学期名
                    if (!isBlank(dto.getSemesterName())) {
                        String semesterName = dto.getSemesterName().trim();
                        LambdaQueryWrapper<Semester> semesterQueryWrapper = new LambdaQueryWrapper<>();
                        semesterQueryWrapper.eq(Semester::getSemesterName, semesterName);
                        Semester semester = semesterService.getOne(semesterQueryWrapper);
                        // 验证学期是否存在
                        if (semester == null) {
                            errorMessages.add(String.format("第%d行上传失败，学期'%s'不存在", rowNum, semesterName));
                            failCount++;
                            continue;
                        }
                        courseSchedule.setSemesterName(semesterName);
                    } else {
                        courseSchedule.setSemesterName(null);
                    }
                    
                    // 处理班级列表
                    List<Class> successClasses = new ArrayList<>();
                    List<String> failedClasses = new ArrayList<>();
                    
                    if (!isBlank(dto.getClassList())) {
                        String[] classArray = dto.getClassList().trim().split(",");
                        
                        for (String className : classArray) {
                            className = className.trim();
                            if (className.isEmpty()) {
                                continue;
                            }
                            
                            // 查询班级是否存在
                            LambdaQueryWrapper<Class> classQueryWrapper = new LambdaQueryWrapper<>();
                            classQueryWrapper.eq(Class::getClassName, className);
                            Class classEntity = classService.getOne(classQueryWrapper);
                            
                            if (classEntity == null) {
                                failedClasses.add(className);
                            } else {
                                successClasses.add(classEntity);
                            }
                        }
                    }
                    
                    // 使用统一的addCourseSchedule方法
                    addCourseSchedule(courseSchedule);
                    
                    // 保存课程与班级的关联关系
                    for (Class classEntity : successClasses) {
                        Course course = new Course();
                        course.setCourseId(courseSchedule.getId());
                        course.setClassId(classEntity.getId());
                        courseService.save(course);
                    }
                    
                    successCount++;
                    
                    // 如果有失败的班级，添加提示信息
                    if (!failedClasses.isEmpty()) {
                        String failedClassNames = String.join(",", failedClasses);
                        errorMessages.add(String.format("第%d行：%s不存在，导入失败，其余班级正常导入", rowNum, failedClassNames));
                    }
                    
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错: {}", rowNum, e.getMessage(), e);
                    errorMessages.add(String.format("第%d行上传失败,请检查该行数据", rowNum));
                    failCount++;
                }
            }
            
            log.info("导入完成，成功：{}条，失败：{}条", successCount, failCount);
            
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", excelDataList.size());
            result.put("message", String.format("成功导入%d条课表数据，失败%d条", successCount, failCount));
            
            if (!errorMessages.isEmpty()) {
                result.put("errors", errorMessages);
            }
            
            return result;
            
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new IllegalArgumentException("读取Excel文件失败: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // 重新抛出参数异常
            throw e;
        } catch (Exception e) {
            log.error("导入课表数据失败", e);
            throw new IllegalStateException("导入课表数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从字符串中提取第一个数字
     * @param str 包含数字的字符串
     * @return 提取的数字，如果未找到则返回null
     */
    private Integer extractNumberFromString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        // 使用正则表达式查找第一个数字序列
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }
        
        return null;
    }
    
    /**
     * 格式化教室名称，将类似"思学楼C506"转换为"成都校区/思学楼/思学楼C506"
     * @param classroom 原始教室名称
     * @return 格式化后的教室名称
     */
    private String formatClassroom(String classroom) {
        if (classroom == null || classroom.trim().isEmpty()) {
            return classroom;
        }
        
        classroom = classroom.trim();
        
        // 提取楼名和房间号
        String building = "";
        String roomNumber = "";
        
        // 查找数字开始的位置，将字符串分为楼名和房间号
        int numberStartIndex = -1;
        for (int i = 0; i < classroom.length(); i++) {
            if (Character.isDigit(classroom.charAt(i))) {
                numberStartIndex = i;
                break;
            }
        }
        
        if (numberStartIndex > 0) {
            // 分离楼名和房间号
            building = classroom.substring(0, numberStartIndex);
            roomNumber = classroom.substring(numberStartIndex);
        } else {
            // 如果没有找到数字，将整个字符串作为楼名
            building = classroom;
        }
        
        // 格式化为"成都校区/楼名/楼名+房间号"
        if (!roomNumber.isEmpty()) {
            return "成都校区/" + building + "/" + building + roomNumber;
        } else {
            return "成都校区/" + building + "/" + building;
        }
    }
    
    /**
     * 将CourseSchedule实体转换为CourseScheduleVO
     */
    private CourseScheduleVO convertToVO(CourseSchedule courseSchedule) {
        CourseScheduleVO vo = new CourseScheduleVO();
        vo.setId(courseSchedule.getId());
        vo.setCourseNo(courseSchedule.getCourseNo());
        vo.setOrderNo(courseSchedule.getOrderNo());
        vo.setCourseName(courseSchedule.getCourseName());
        vo.setWeekday(courseSchedule.getWeekday());
        vo.setWeekRange(courseSchedule.getWeekRange());
        vo.setStartPeriod(courseSchedule.getStartPeriod());
        vo.setEndPeriod(courseSchedule.getEndPeriod());
        vo.setClassroom(courseSchedule.getClassroom());
        vo.setExpectedCount(courseSchedule.getExpectedCount());
        vo.setTeacherName(courseSchedule.getTeacherName());
        vo.setCourseType(courseSchedule.getCourseType());
        vo.setSemesterName(courseSchedule.getSemesterName());
        vo.setCreateTime(courseSchedule.getCreateTime());
        vo.setUpdateTime(courseSchedule.getUpdateTime());
        
        // 查询并设置关联的班级信息
        List<Class> classes = getClassNamesByCourseId(courseSchedule.getId());
        vo.setClasses( classes);
        //设置是否在上课时间
        vo.setInClassTime(isInClassTime(courseSchedule));
        return vo;
    }

    private Boolean isInClassTime(CourseSchedule courseSchedule) {
        LocalDateTime now = LocalDateTime.now();
        //星期几匹配
        String weekday = ClassTimeUtil.convertDayOfWeekToChinese(now.getDayOfWeek());
        if(!weekday.equals(courseSchedule.getWeekday())){
            log.info("星期不匹配 当前星期：{}，星期：{}",weekday,courseSchedule.getWeekday());
            return false;
        }
        //节次匹配
        if(!(ClassTimeUtil.getClassNumberByTime(now.toLocalTime())>courseSchedule.getStartPeriod()&&ClassTimeUtil.getClassNumberByTime(now.toLocalTime())<courseSchedule.getEndPeriod())){
            log.info("不在节次范围内 当前节次：{}，开始节次：{}，结束节次：{}",ClassTimeUtil.getClassNumberByTime(now.toLocalTime()),courseSchedule.getStartPeriod(),courseSchedule.getEndPeriod());
            return false;
        }
        return true;
    }

    /**
     * 根据课程ID获取关联的班级名称列表
     */
    private List<Class> getClassNamesByCourseId(String courseId) {
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getCourseId, courseId);
        
        List<Course> courseList = courseService.list(courseQueryWrapper);
        List<String> classIds = courseList.stream()
                .map(Course::getClassId)
                .collect(Collectors.toList());
        
        if (!classIds.isEmpty()) {
            LambdaQueryWrapper<Class> classQueryWrapper = new LambdaQueryWrapper<>();
            classQueryWrapper.in(Class::getId, classIds);
            List<Class> classList = classService.list(classQueryWrapper);
            return classList;
        }
        
        return new ArrayList<>();
    }

    private List<String> getClassIdsByCourseId(String courseId){
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getCourseId, courseId);
        List<Course> courseList = courseService.list(courseQueryWrapper);
        List<String> classIds = courseList.stream()
                .map(Course::getClassId)
                .collect(Collectors.toList());
        return classIds;
    }
    
    @Override
    public Page<CourseScheduleVO> queryPage(CourseScheduleQueryDTO queryDTO) {
        // 设置分页参数
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
        
        // 构建查询条件
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        
        // 获取当前登录用户的角色
        Object roleObj = StpUtil.getSession().get("role");
        String currentRole = roleObj != null ? roleObj.toString() : null;

        List<String> teacherNos = new ArrayList<>();

        // 根据不同角色获取教师工号列表
        if ("teacher".equals(currentRole)) {
            // 如果是教师角色，直接使用当前登录教师工号
            teacherNos = getTeacherNosForTeacherRole(queryDTO);
        } else if ("college_admin".equals(currentRole)) {
            // 如果是学院管理员角色，获取该学院的所有教师工号
            teacherNos = getTeacherNosForCollegeAdminRole(queryDTO);
        } else if ("admin".equals(currentRole)) {
            // 如果是学校管理员角色，根据条件获取教师工号
            teacherNos = getTeacherNosForAdminRole(queryDTO);
        } else {
            log.error("当前用户角色无效");
        }
        
        if (teacherNos.isEmpty()) {
            // 如果没有找到对应的教师工号，返回空结果
            Page<CourseScheduleVO> emptyPage = new Page<>();
            emptyPage.setTotal(0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
        
        // 获取这些教师关联的班级
        QueryWrapper<Class> classQueryWrapper = new QueryWrapper<>();
        classQueryWrapper.in("teacher_no", teacherNos);
        
        List<Class> classList = classService.list(classQueryWrapper);
        
        // 如果有班级名称条件，则剔除其中无效班级
        if (!isBlank(queryDTO.getClassName())) {
            classList = classList.stream()
                    .filter(classEntity -> classEntity.getClassName().contains(queryDTO.getClassName()))
                    .collect(Collectors.toList());
        }
        
        if(classList.isEmpty()){
            Page<CourseScheduleVO> emptyPage = new Page<>();
            emptyPage.setTotal(0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
        
        List<String> classIds = classList.stream()
                .map(Class::getId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.in(Course::getClassId, classIds);
        List<Course> courseList = courseService.list(courseQueryWrapper);
        List<String> courseIds = courseList.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList());
        
        if(courseIds.isEmpty()){
            Page<CourseScheduleVO> emptyPage = new Page<>();
            emptyPage.setTotal(0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
        
        queryWrapper.in(CourseSchedule::getId, courseIds);
        
        // 课程名称条件（模糊查询）
        if (!isBlank(queryDTO.getCourseName())) {
            queryWrapper.like(CourseSchedule::getCourseName, queryDTO.getCourseName().trim());
        }
        
        // 任课老师条件（模糊查询）
        if (!isBlank(queryDTO.getTeacherName())) {
            queryWrapper.like(CourseSchedule::getTeacherName, queryDTO.getTeacherName().trim());
        }
        
        // 课程类型条件（精确查询）
        if (!isBlank(queryDTO.getCourseType())) {
            queryWrapper.eq(CourseSchedule::getCourseType, queryDTO.getCourseType().trim());
        }
        
        // 学期名条件（模糊查询）
        if (!isBlank(queryDTO.getSemesterName())) {
            queryWrapper.like(CourseSchedule::getSemesterName, queryDTO.getSemesterName().trim());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(CourseSchedule::getCreateTime);
        
        log.info("查询课表，条件：teacherNo={}, className={}, courseName={}, teacherName={}, courseType={}, semesterName={}, pageNum={}, pageSize={}", 
                queryDTO.getTeacherNo(), queryDTO.getClassName(), queryDTO.getCourseName(), queryDTO.getTeacherName(), queryDTO.getCourseType(), 
                queryDTO.getSemesterName(), pageNum, pageSize);
        
        // 执行查询获取CourseSchedule分页结果
        Page<CourseSchedule> courseSchedulePage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        
        // 转换为VO分页结果
        Page<CourseScheduleVO> voPage = new Page<>();
        voPage.setCurrent(courseSchedulePage.getCurrent());
        voPage.setSize(courseSchedulePage.getSize());
        voPage.setTotal(courseSchedulePage.getTotal());
        
        List<CourseScheduleVO> voList = courseSchedulePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 为教师角色获取教师工号列表
     * @param queryDTO 查询条件
     * @return 教师工号列表
     */
    private List<String> getTeacherNosForTeacherRole(CourseScheduleQueryDTO queryDTO) {
        List<String> teacherNos = new ArrayList<>();
        // 如果当前角色为教师，则直接使用该教师工号
        if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().isEmpty()) {
            // 如果指定了教师工号，则使用指定的教师工号（权限检查由业务逻辑完成）
            teacherNos.add(queryDTO.getTeacherNo());
        } else {
            // 如果没有指定教师工号，使用当前登录教师工号
            if (StpUtil.isLogin()) {
                String currentLoginId = StpUtil.getLoginIdAsString();
                teacherNos.add(currentLoginId);
            }
        }
        return teacherNos;
    }

    /**
     * 为学院管理员角色获取教师工号列表
     * @param queryDTO 查询条件
     * @return 教师工号列表
     */
    private List<String> getTeacherNosForCollegeAdminRole(CourseScheduleQueryDTO queryDTO) {
        List<String> teacherNos = new ArrayList<>();
        // 如果当前角色为学院管理员，则学院条件指定为该学院管理员的学院
        CollegeAdmin collegeAdmin = (CollegeAdmin) StpUtil.getSession().get("userInfo");
        if (collegeAdmin != null) {
            // 判断有无教师工号条件
            if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().isEmpty()) {
                // 若有，则直接使用该教师工号
                teacherNos.add(queryDTO.getTeacherNo());
            } else {
                // 否则执行按学院获取教师工号列表方法
                LambdaQueryWrapper<College> collegeQueryWrapper = new LambdaQueryWrapper<>();
                collegeQueryWrapper.eq(College::getId, collegeAdmin.getCollegeId());
                College college = collegeService.getOne(collegeQueryWrapper);
                if (college != null) {
                    teacherNos = teacherMapper.queryTeacherNoByCollegeName(college.getName());
                }
            }
        }
        return teacherNos;
    }

    /**
     * 为学校管理员角色获取教师工号列表
     * @param queryDTO 查询条件
     * @return 教师工号列表
     */
    private List<String> getTeacherNosForAdminRole(CourseScheduleQueryDTO queryDTO) {
        List<String> teacherNos = new ArrayList<>();
        // 当前角色为学校管理员的时候
        // 先判断有无教师工号条件，若有则直接使用该教师工号
        if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().isEmpty()) {
            teacherNos.add(queryDTO.getTeacherNo());
        } else if (queryDTO.getCollegeName() != null && !queryDTO.getCollegeName().isEmpty()) {
            // 再判断有无学院条件，若有则直接使用按学院获得教师工号
            teacherNos = teacherMapper.queryTeacherNoByCollegeName(queryDTO.getCollegeName());
        } else {
            // 都没有则获取所有教师工号
            List<Teacher> teacherList = teacherService.list();
            if (teacherList != null && !teacherList.isEmpty()) {
                teacherNos = teacherList.stream()
                        .map(Teacher::getTeacherNo)
                        .collect(Collectors.toList());
            }
        }
        return teacherNos;
    }

    @Override
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("课表导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            // 创建模板数据
            List<CourseScheduleExcelDTO> templateData = new ArrayList<>();
            CourseScheduleExcelDTO example = new CourseScheduleExcelDTO();
            example.setCourseNo("MATH101");
            example.setCourseName("高等数学");
            example.setOrderNo("01");
            example.setWeekRange("3-16周");
            example.setWeekday("星期一");
            example.setStartPeriod("1");
            example.setEndPeriod("2");
            example.setClassroom("思学楼A101");
            example.setClassList("25计算机类-1班,25计算机类-2班");
            example.setTeacherName("张老师");
            example.setCourseType("专业课");
            example.setExpectedCount("90");
            example.setSemesterName("2025-2026学年春季学期");
            templateData.add(example);
            
            // 写入Excel
            EasyExcel.write(response.getOutputStream(), CourseScheduleExcelDTO.class)
                    .sheet("课表模板")
                    .doWrite(templateData);
            
            log.info("课表导入模板下载成功");
        } catch (Exception e) {
            log.error("生成课表导入模板失败", e);
            throw new com.xuegongbu.common.exception.BusinessException("生成课表导入模板失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> addClassByIds(List<String> classList,String courseId) {
       LambdaQueryWrapper<Class> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Class::getId, classList);
        List<Class> classes = classMapper.selectList(queryWrapper);
        int successCount = 0;

        for(Class clazz:classes){
            Course course = new Course();
            course.setCourseId(courseId);
            course.setClassId(clazz.getId());
            if(courseService.save(course)){
                log.info("添加班级{}成功", clazz.getClassName());
                successCount++;
            };
        }
        int failCount = classList.size() - successCount;
        return Result.success(String.format("成功添加%d个班级，失败%d个", successCount, failCount));
    }

    /**
     * 根据班级ID查询课表
     * @param id 班级ID
     * @return 课表列表
     */
    @Override
    public Page<CourseScheduleVO> queryByClass(String id, int pageNum, int pageSize) {
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CourseSchedule> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("class_id", id);
        List<Course> courseList = courseService.list(courseQueryWrapper);
        List<String> courseIds = courseList.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList());
        if(courseIds.isEmpty()){
            return new Page<>();
        }
        queryWrapper.in("id", courseIds);
        queryWrapper.orderByDesc("create_time");
        Page<CourseSchedule> courseSchedulePage = this.page(page, queryWrapper);
        // 转换为VO分页结果
        Page<CourseScheduleVO> voPage = new Page<>();
        voPage.setCurrent(courseSchedulePage.getCurrent());
        voPage.setSize(courseSchedulePage.getSize());
        voPage.setTotal(courseSchedulePage.getTotal());

        List<CourseScheduleVO> voList = courseSchedulePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<String> queryClassCurrentCourse(String classId) {
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getClassId, classId);
        List<Course> courseList = courseService.list(courseQueryWrapper);
        List<String> courseIds = courseList.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList());
        if(courseIds.isEmpty()){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<CourseSchedule> courseScheduleQueryWrapper = new LambdaQueryWrapper<>();
        courseScheduleQueryWrapper.in(CourseSchedule::getId, courseIds);
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        Integer period = ClassTimeUtil.getClassNumberByTime(now.toLocalTime());
        //必须是当前星期
        courseScheduleQueryWrapper.eq(CourseSchedule::getWeekday, ClassTimeUtil.convertDayOfWeekToChinese(now.getDayOfWeek()));
        //开始节次<=当前节次<=结束节次
        courseScheduleQueryWrapper.ge(CourseSchedule::getStartPeriod, period);
        courseScheduleQueryWrapper.le(CourseSchedule::getEndPeriod, period);
        List<CourseSchedule> courseScheduleList = this.list(courseScheduleQueryWrapper);
        if(courseScheduleList.isEmpty()){
            return new ArrayList<>();
        }
        return courseScheduleList.stream()
                .map(CourseSchedule::getCourseName)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteClassByIds(List<String> classList, String id) {
        LambdaQueryWrapper<Class> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Class::getId, classList);
        List<Class> classes = classMapper.selectList(queryWrapper);
        int successCount = 0;
        for(Class clazz:classes){
            LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
            courseQueryWrapper.eq(Course::getClassId, clazz.getId());
            courseQueryWrapper.eq(Course::getCourseId, id);
            if(courseService.remove(courseQueryWrapper)){
                log.info("成功删除班级：{}", clazz.getClassName());
                successCount++;
            }
        }
        int failCount = classList.size() - successCount;
        log.info("成功删除%d个班级，失败%d个", successCount, failCount);
    }

    @Override
    public CourseScheduleVO getCourseScheduleById(String id) {
        CourseSchedule courseSchedule = this.getById(id);
        return convertToVO(courseSchedule);
    }

    @Override
    public Result<String> updateCourseSchedule(CourseScheduleWithClassIdsDTO courseScheduleDTO) {
        CourseSchedule courseSchedule = courseScheduleDTO.getCourseSchedule();
        log.info("更新课表，课程名称：{}，课表信息：{}",
                courseSchedule.getCourseName(), courseSchedule);

        if (courseSchedule.getCourseName() == null || courseSchedule.getCourseName().trim().isEmpty()) {
            return Result.error("课程名称不能为空");
        }

        // 根据课程名称查询课表（班级信息现在通过关联表获取）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCourseName, courseSchedule.getCourseName().trim());

        CourseSchedule existing =this.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("课表不存在或无权限修改");
        }

        // 设置ID以便更新
        courseSchedule.setId(existing.getId());

       this.updateById(courseSchedule);

        //更新班级信息
        if(courseScheduleDTO.getClassIds() != null&& !courseScheduleDTO.getClassIds().isEmpty()){
            // 删除原来的班级信息
            List<String> classIds = this.getClassIdsByCourseId(existing.getId());
            deleteClassByIds(classIds, existing.getId());
            addClassByIds(courseScheduleDTO.getClassIds(), existing.getId());
            log.info("更新班级信息完成");
        }
        log.info("更新课表完成");
        return Result.success("更新成功");
    }
}
