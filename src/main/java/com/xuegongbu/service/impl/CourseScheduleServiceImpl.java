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
import com.xuegongbu.dto.CourseScheduleAddRequest;
import com.xuegongbu.dto.CourseScheduleExcelDTO;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.dto.CourseScheduleVO;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
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

    /**
     * 添加单个课程
     * 处理课程基本信息的保存和班级关联
     * @param request 课程信息（与Excel模板字段一致）
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<CourseSchedule> addCourseSchedule(CourseScheduleAddRequest request) {
        try {
            // 验证必填字段
            if (isBlank(request.getKcm())) {
                return Result.error("课程名称不能为空");
            }
            if (isBlank(request.getKch())) {
                return Result.error("课程号不能为空");
            }
            if (isBlank(request.getKxh())) {
                return Result.error("课序号不能为空");
            }
            if (isBlank(request.getSkxq())) {
                return Result.error("上课星期不能为空");
            }
            if (!isValidWeekday(request.getSkxq())) {
                return Result.error("上课星期格式不正确，应为：星期一、星期二、星期三、星期四、星期五、星期六、星期日");
            }
            if (isBlank(request.getZcmc())) {
                return Result.error("上课周次不能为空");
            }
            if (request.getKsjc() == null || request.getKsjc() < 1 || request.getKsjc() > 12) {
                return Result.error("开始节次必须是1-12之间的数字");
            }
            if (request.getJsjc() == null || request.getJsjc() < 1 || request.getJsjc() > 12) {
                return Result.error("结束节次必须是1-12之间的数字");
            }
            if (request.getJsjc() < request.getKsjc()) {
                return Result.error("结束节次必须大于或等于开始节次");
            }
            if (isBlank(request.getJasmc())) {
                return Result.error("上课教室名不能为空");
            }

            // 创建课表对象
            CourseSchedule courseSchedule = new CourseSchedule();
            courseSchedule.setCourseNo(request.getKch().trim());
            courseSchedule.setCourseName(request.getKcm().trim());
            courseSchedule.setOrderNo(request.getKxh().trim());
            courseSchedule.setWeekRange(request.getZcmc().trim());
            courseSchedule.setWeekday(request.getSkxq().trim());
            courseSchedule.setStartPeriod(request.getKsjc());
            courseSchedule.setEndPeriod(request.getJsjc());
            courseSchedule.setClassroom(request.getJasmc().trim());
            courseSchedule.setTeacherName(isBlank(request.getRkls()) ? null : request.getRkls().trim());
            courseSchedule.setCourseType(isBlank(request.getKclx()) ? null : request.getKclx().trim());
            
            // 处理预到人数：如果提供了预到人数且大于0，使用提供的值；否则根据班级计算
            Integer providedExpectedCount = request.getYdrs();
            
            // 处理班级列表
            List<String> failedClasses = new ArrayList<>();
            List<Class> successClasses = new ArrayList<>();
            int totalExpectedCount = 0;
            
            if (!isBlank(request.getWzskbj())) {
                String[] classArray = request.getWzskbj().trim().split(",");
                
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
                        totalExpectedCount += classEntity.getCount();
                    }
                }
            }
            
            // 设置预到人数：优先使用提供的值，否则使用班级人数总和
            if (providedExpectedCount != null && providedExpectedCount > 0) {
                courseSchedule.setExpectedCount(providedExpectedCount);
            } else {
                courseSchedule.setExpectedCount(totalExpectedCount);
            }
            
            // 保存课表
            this.save(courseSchedule);
            
            // 保存课程与班级的关联关系
            for (Class classEntity : successClasses) {
                Course course = new Course();
                course.setCourseId(courseSchedule.getId());
                course.setClassId(classEntity.getId());
                courseService.save(course);
            }
            
            // 返回结果，如果有失败的班级，在消息中说明
            Result<CourseSchedule> result = Result.success(courseSchedule);
            if (!failedClasses.isEmpty()) {
                String failedClassNames = String.join(",", failedClasses);
                result.setMessage(failedClassNames + "不存在，导入失败，其余班级正常导入");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("添加课程失败", e);
            return Result.error("添加课程失败: " + e.getMessage());
        }
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
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：课程号不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getCourseName())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：课程名称不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getOrderNo())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：课序号不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getWeekRange())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：上课周次不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getWeekday())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：上课星期不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    if (!isValidWeekday(dto.getWeekday())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：上课星期格式不正确", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 验证并转换节次
                    Integer startPeriod = extractNumberFromString(dto.getStartPeriod());
                    Integer endPeriod = extractNumberFromString(dto.getEndPeriod());
                    
                    if (startPeriod == null || startPeriod < 1 || startPeriod > 12) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：开始节次必须是1-12之间的数字", rowNum));
                        failCount++;
                        continue;
                    }
                    if (endPeriod == null || endPeriod < 1 || endPeriod > 12) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：结束节次必须是1-12之间的数字", rowNum));
                        failCount++;
                        continue;
                    }
                    if (startPeriod > endPeriod) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：结束节次必须大于或等于开始节次", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    if (isBlank(dto.getClassroom())) {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：上课教室名不能为空", rowNum));
                        failCount++;
                        continue;
                    }
                    
                    // 转换DTO为请求对象
                    CourseScheduleAddRequest request = new CourseScheduleAddRequest();
                    request.setKch(dto.getCourseNo());
                    request.setKcm(dto.getCourseName());
                    request.setKxh(dto.getOrderNo());
                    request.setZcmc(dto.getWeekRange());
                    request.setSkxq(dto.getWeekday());
                    request.setKsjc(startPeriod);
                    request.setJsjc(endPeriod);
                    request.setJasmc(dto.getClassroom());
                    request.setWzskbj(dto.getClassList());
                    request.setRkls(dto.getTeacherName());
                    request.setKclx(dto.getCourseType());
                    
                    // 转换预到人数
                    Integer expectedCount = extractNumberFromString(dto.getExpectedCount());
                    request.setYdrs(expectedCount);
                    
                    // 调用添加课程方法
                    Result<CourseSchedule> addResult = addCourseSchedule(request);
                    
                    if (addResult.getCode() == 0) {
                        successCount++;
                        // 如果有部分班级不存在的提示，添加到错误消息中
                        if (addResult.getMessage() != null && addResult.getMessage().contains("不存在")) {
                            errorMessages.add(String.format("第%d行：%s", rowNum, addResult.getMessage()));
                        }
                    } else {
                        errorMessages.add(String.format("第%d行上传失败,请检查该行数据：%s", rowNum, addResult.getMessage()));
                        failCount++;
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
     * 验证星期几格式是否正确
     * @param weekday 星期几（汉字格式）
     * @return 是否有效
     */
    private boolean isValidWeekday(String weekday) {
        if (weekday == null) {
            return false;
        }
        String[] validWeekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        for (String validWeekday : validWeekdays) {
            if (validWeekday.equals(weekday.trim())) {
                return true;
            }
        }
        return false;
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
        vo.setCreateTime(courseSchedule.getCreateTime());
        vo.setUpdateTime(courseSchedule.getUpdateTime());
        
        // 查询并设置关联的班级信息
        List<String> classNames = getClassNamesByCourseId(courseSchedule.getId());
        vo.setClassNames(classNames);
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
    private List<String> getClassNamesByCourseId(String courseId) {
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
            return classList.stream()
                    .map(Class::getClassName)
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
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

        if ("college_admin".equals(currentRole)) {
            // 如果是学院管理员，获取该学院的所有教师工号
            if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().isEmpty()) {
                // 如果指定了教师工号，则只查询该教师的课表（回退到教师查询逻辑）
                teacherNos.add(queryDTO.getTeacherNo());
            } else {
                // 否则查询该学院管理员管理的所有教师的课表
                String currentLoginId = StpUtil.getLoginIdAsString();

                // 查询学院管理员信息
                LambdaQueryWrapper<CollegeAdmin> collegeAdminQueryWrapper = new LambdaQueryWrapper<>();
                collegeAdminQueryWrapper.eq(CollegeAdmin::getId, currentLoginId);
                CollegeAdmin collegeAdmin = collegeAdminService.getOne(collegeAdminQueryWrapper);

                if (collegeAdmin != null) {
                    // 根据学院ID查询学院号
                    LambdaQueryWrapper<College> collegeQueryWrapper = new LambdaQueryWrapper<>();
                    collegeQueryWrapper.eq(College::getId, collegeAdmin.getCollegeId());
                    College college = collegeService.getOne(collegeQueryWrapper);
                    if (college != null) {
                        // 根据学院号查询教师工号
                        LambdaQueryWrapper<Teacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
                        teacherQueryWrapper.eq(Teacher::getCollegeNo, college.getCollegeNo());
                        List<Teacher> teacherList = teacherService.list(teacherQueryWrapper);
                        if (teacherList != null && !teacherList.isEmpty()) {
                            teacherNos = teacherList.stream()
                                    .map(Teacher::getTeacherNo)
                                    .collect(Collectors.toList());
                        }
                    }
                }
            }
        } else {
            // 如果是教师或其他角色，使用原有的逻辑
            if (queryDTO.getTeacherNo() != null && !queryDTO.getTeacherNo().isEmpty()) {
                teacherNos.add(queryDTO.getTeacherNo());
            } else {
                // 如果没有指定教师工号，尝试从当前登录教师获取
                if (StpUtil.isLogin()) {
                    String currentLoginId = StpUtil.getLoginIdAsString();
                    teacherNos.add(currentLoginId); // 假设登录ID是教师工号
                }
            }
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
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(CourseSchedule::getCreateTime);
        
        log.info("查询课表，条件：teacherNo={}, className={}, courseName={}, teacherName={}, courseType={}, pageNum={}, pageSize={}", 
                queryDTO.getTeacherNo(), queryDTO.getClassName(), queryDTO.getCourseName(), queryDTO.getTeacherName(), queryDTO.getCourseType(), pageNum, pageSize);
        
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
    public Result<String> addClass(List<String> classList,String courseId) {
        QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("className", classList);
        List<Class> classes = classMapper.selectList(queryWrapper);
        int successCount = classes.size();
        int failCount = classList.size() - successCount;
        for(Class clazz:classes){
            Course course = new Course();
            course.setCourseId(courseId);
            course.setClassId(clazz.getId());
            courseService.save(course);
        }
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
}
