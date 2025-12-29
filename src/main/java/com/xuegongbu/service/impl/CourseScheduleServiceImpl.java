package com.xuegongbu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.CourseSchedule;
import com.xuegongbu.dto.CourseScheduleExcelDTO;
import com.xuegongbu.dto.CourseScheduleQueryDTO;
import com.xuegongbu.dto.CourseScheduleVO;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.mapper.ClassMapper;
import com.xuegongbu.mapper.CourseScheduleMapper;
import com.xuegongbu.domain.Class;
import com.xuegongbu.domain.Course;
import com.xuegongbu.service.ClassService;
import com.xuegongbu.service.CollegeAdminService;
import com.xuegongbu.service.CourseService;
import com.xuegongbu.service.CourseScheduleService;
import com.xuegongbu.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            // 读取Excel数据
            List<CourseScheduleExcelDTO> excelDataList = EasyExcel.read(file.getInputStream())
                    .head(CourseScheduleExcelDTO.class)
                    .sheet()
                    .doReadSync();
            
            if (excelDataList == null || excelDataList.isEmpty()) {
                throw new IllegalArgumentException("Excel文件中没有数据");
            }
            
            log.info("从Excel读取到 {} 条数据", excelDataList.size());
            
            // 转换为CourseSchedule对象并保存
            List<CourseSchedule> courseScheduleList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (int i = 0; i < excelDataList.size(); i++) {
                try {
                    CourseScheduleExcelDTO dto = excelDataList.get(i);
                    
                    // 验证必填字段
                    if (isBlank(dto.getCourseName())) {
                        errorMessages.add(String.format("第%d行：课程名称不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getWeekday())) {
                        errorMessages.add(String.format("第%d行：星期几不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (!isValidWeekday(dto.getWeekday())) {
                        errorMessages.add(String.format("第%d行：星期几格式不正确，应为：星期一、星期二、星期三、星期四、星期五、星期六、星期日", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getWeekRange())) {
                        errorMessages.add(String.format("第%d行：周次范围不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getStartPeriod() == null || dto.getStartPeriod() < 1 || dto.getStartPeriod() > 12) {
                        errorMessages.add(String.format("第%d行：开始节次必须是1-12之间的数字", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getEndPeriod() == null || dto.getEndPeriod() < 1 || dto.getEndPeriod() > 12) {
                        errorMessages.add(String.format("第%d行：结束节次必须是1-12之间的数字", i + 2));
                        failCount++;
                        continue;
                    }
                    if (dto.getEndPeriod() < dto.getStartPeriod()) {
                        errorMessages.add(String.format("第%d行：结束节次必须大于或等于开始节次", i + 2));
                        failCount++;
                        continue;
                    }
                    if (isBlank(dto.getClassroom())) {
                        errorMessages.add(String.format("第%d行：教室不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    
                    CourseSchedule courseSchedule = new CourseSchedule();

                    // 验证并处理班级列表
                    String[] classArray = null;
                    if (dto.getClassList() != null && !dto.getClassList().trim().isEmpty()) {
                        classArray = dto.getClassList().trim().split("[,，]"); // 支持中文逗号和英文逗号分隔
                    }
                    
                    if (classArray == null || classArray.length == 0) {
                        errorMessages.add(String.format("第%d行：上课班级不能为空", i + 2));
                        failCount++;
                        continue;
                    }
                    
                    // 验证班级是否存在
                    List<Class> classEntities = new ArrayList<>();
                    int totalExpectedCount = 0;
                    for (String className : classArray) {
                        className = className.trim();
                        if (className.isEmpty()) {
                            continue;
                        }
                        
                        LambdaQueryWrapper<Class> classQueryWrapper = new LambdaQueryWrapper<>();
                        classQueryWrapper.eq(Class::getClassName, className);
                        Class classEntity = classService.getOne(classQueryWrapper);
                        
                        if (classEntity == null) {
                            errorMessages.add(String.format("第%d行：班级 '%s' 不存在", i + 2, className));
                            failCount++;
                            continue;
                        }
                        
                        classEntities.add(classEntity);
                        totalExpectedCount += classEntity.getCount();
                    }
                    
                    if (classEntities.isEmpty()) {
                        // 如果所有班级都不存在，则跳过此行
                        continue;
                    }
                    
                    // 设置课表基本信息
                    courseSchedule.setCourseNo(isBlank(dto.getCourseNo()) ? null : dto.getCourseNo().trim());
                    courseSchedule.setOrderNo(isBlank(dto.getOrderNo()) ? null : dto.getOrderNo().trim());
                    courseSchedule.setCourseName(dto.getCourseName().trim());
                    courseSchedule.setWeekday(dto.getWeekday().trim());
                    courseSchedule.setWeekRange(dto.getWeekRange().trim());
                    courseSchedule.setStartPeriod(dto.getStartPeriod());
                    courseSchedule.setEndPeriod(dto.getEndPeriod());
                    courseSchedule.setClassroom(dto.getClassroom().trim());
                    courseSchedule.setExpectedCount(totalExpectedCount); // 设置预到人数为所有班级人数之和
                    
                    // 先保存课表获取ID
                    this.save(courseSchedule);
                    
                    // 保存课程与班级的关联关系
                    for (Class classEntity : classEntities) {
                        Course course = new Course();
                        course.setCourseId(courseSchedule.getId()); // 使用课表ID作为课程ID
                        course.setClassId(classEntity.getId());
                        courseService.save(course);
                    }
                    
                    successCount++; // 现在可以增加成功计数
                } catch (Exception e) {
                    log.error("处理第{}行数据时出错: {}", i + 2, e.getMessage(), e);
                    errorMessages.add(String.format("第%d行：%s", i + 2, e.getMessage()));
                    failCount++;
                }
            }

            // 由于每个课表都需要单独处理关联关系，所以不需要批量保存courseScheduleList
            // 课表已经在循环中单独保存了
            
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
     * 检查字符串是否为空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
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
        vo.setCreateTime(courseSchedule.getCreateTime());
        vo.setUpdateTime(courseSchedule.getUpdateTime());
        
        // 查询并设置关联的班级信息
        List<String> classNames = getClassNamesByCourseId(courseSchedule.getId());
        vo.setClassNames(classNames);
        
        return vo;
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
                collegeAdminQueryWrapper.eq(CollegeAdmin::getUsername, currentLoginId); // 假设登录ID是用户名
                CollegeAdmin collegeAdmin = collegeAdminService.getOne(collegeAdminQueryWrapper);
                
                if (collegeAdmin != null) {
                    // 根据学院ID查询该学院的所有教师工号
                    LambdaQueryWrapper<Teacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
                    teacherQueryWrapper.eq(Teacher::getCollegeNo, collegeAdmin.getCollegeId());
                    List<Teacher> teachers = teacherService.list(teacherQueryWrapper);
                    teacherNos = teachers.stream()
                            .map(Teacher::getTeacherNo)
                            .collect(Collectors.toList());
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
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(CourseSchedule::getCreateTime);
        
        log.info("查询课表，条件：teacherNo={}, className={}, courseName={}, pageNum={}, pageSize={}", 
                queryDTO.getTeacherNo(), queryDTO.getClassName(), queryDTO.getCourseName(), pageNum, pageSize);
        
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
            example.setCourseName("高等数学");
            example.setCourseNo("MATH101");
            example.setOrderNo("01");
            example.setWeekday("星期一"); // 星期一（汉字格式）
            example.setWeekRange("3-16周"); // 周次范围
            example.setStartPeriod(1);
            example.setEndPeriod(2);
            example.setClassroom("成都校区/思学楼/A101");
            example.setClassList("25计算机类-1班,25计算机类-2班");
            // 不再包含教师ID，将由系统根据当前登录教师自动填充
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
    public Page<CourseSchedule> queryByClass(String id, int pageNum, int pageSize) {
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CourseSchedule> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.eq("class_id", id);
        List<Course> courseList = courseService.list(courseQueryWrapper);
        List<String> courseIds = courseList.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList());
        queryWrapper.in("id", courseIds);
        return this.page(page, queryWrapper);
    }
}
