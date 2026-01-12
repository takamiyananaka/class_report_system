package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.TeacherExcelDTO;
import com.xuegongbu.dto.TeacherQueryDTO;
import com.xuegongbu.dto.TeacherRequest;
import com.xuegongbu.mapper.CollegeAdminMapper;
import com.xuegongbu.mapper.CollegeMapper;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.vo.TeacherVO;
import io.netty.util.internal.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/teacher")
@Tag(name = "教师管理", description = "教师相关接口")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private CollegeAdminMapper collegeAdminMapper;

    /**
     * 查询本学院的所有教师
     */
    @GetMapping("/teachers")
    @Operation(summary = "查询本学院的所有教师", description = "学院查询本学院（college_no匹配）的教师列表，学院编号从前端传入")
    @SaCheckRole("college_admin")
    public Result<List<TeacherVO>> listTeachers(@Parameter(description = "学院编号", required = true) @RequestParam("collegeNo") String collegeNo) {
        log.info("学院{}查询本学院的所有教师", collegeNo);

        List<Teacher> teachers = teacherService.lambdaQuery()
                .eq(Teacher::getCollegeNo, collegeNo)
                .list();

        List<TeacherVO> teacherVOList = teachers.stream().map(teacher -> {
            TeacherVO vo = new TeacherVO();
            BeanUtils.copyProperties(teacher, vo);
            return vo;
        }).collect(Collectors.toList());

        log.info("查询到{}个教师", teacherVOList.size());
        return Result.success(teacherVOList);
    }

    /**
     * 根据ID查询本学院的教师
     */
    @GetMapping("/teachers/{id}")
    @Operation(summary = "根据ID查询本学院的教师", description = "学院根据教师ID查询本学院的教师详情，学院编号从前端传入")
    @SaCheckRole("college_admin")
    public Result<TeacherVO> getTeacher(
            @Parameter(description = "教师ID") @PathVariable String id,
            @Parameter(description = "学院编号", required = true) @RequestParam("collegeNo") String collegeNo) {
        log.info("学院{}查询教师详情，ID：{}", collegeNo, id);

        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }

        // 检查教师是否属于本学院
        if (!collegeNo.equals(teacher.getCollegeNo())) {
            return Result.error("无权限查看该教师信息");
        }

        TeacherVO vo = new TeacherVO();
        BeanUtils.copyProperties(teacher, vo);
        return Result.success(vo);
    }

    /**
     * 创建本学院的教师
     */
    @PostMapping("/teachers")
    @Operation(summary = "创建本学院的教师", description = "学院创建本学院的新教师，密码必须至少6位字符，学院编号从前端传入")
    @SaCheckRole("college_admin")
    public Result<String> createTeacher(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @Valid @RequestBody TeacherRequest request,
            @Parameter(description = "学院编号", required = true) @RequestParam("collegeNo") String collegeNo) {
        log.info("创建教师，用户名：{}，学院编号：{}", request.getUsername(), collegeNo);

        // 创建教师对象
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(request, teacher);

        // 使用统一的addTeacher方法
        return teacherService.addTeacher(teacher, collegeNo);
    }

    /**
     * 更新本学院的教师
     */
    @PutMapping("/teachers/{id}")
    @Operation(summary = "更新本学院的教师", description = "学院更新本学院的教师信息，如提供新密码则必须至少6位字符，学院编号从前端传入")
    @SaCheckRole("college_admin")
    public Result<String> updateTeacher(
            @Parameter(description = "教师ID") @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters. RequestBody(description = "教师信息") @Valid @RequestBody TeacherRequest request) {
        String collegeNo = teacherService.getById(id).getCollegeNo();
        log.info("学院{}更新教师，ID：{}", collegeNo, id);

        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }

        // 检查教师是否属于本学院
        if (!collegeNo.equals(teacher.getCollegeNo())) {
            return Result.error("无权限修改该教师信息");
        }

        // 检查用户名是否被其他教师使用
        Teacher existingTeacher = teacherService.lambdaQuery()
                .eq(Teacher::getUsername, request.getUsername())
                .ne(Teacher::getId, id)
                .one();
        if (existingTeacher != null) {
            return Result.error("用户名已被其他教师使用");
        }

        // 检查教师工号是否被其他教师使用
        Teacher existingTeacherNo = teacherService.lambdaQuery()
                .eq(Teacher::getTeacherNo, request.getTeacherNo())
                .ne(Teacher::getId, id)
                .one();
        if (existingTeacherNo != null) {
            return Result.error("教师工号已被其他教师使用");
        }

        if (!StringUtil.isNullOrEmpty(request.getUsername())) {
            teacher.setUsername(request.getUsername());
        }
        if (!StringUtil.isNullOrEmpty(request.getRealName())) {
            teacher.setRealName(request.getRealName());
        }
        if (!StringUtil.isNullOrEmpty(request.getTeacherNo())) {
            teacher.setTeacherNo(request.getTeacherNo());
        }
        if (!StringUtil.isNullOrEmpty(request.getPhone())){
            teacher.setPhone(request.getPhone());
        }
        if(!StringUtil.isNullOrEmpty(request.getEmail())){
            teacher.setEmail(request.getEmail());
        }
        if(request.getEnableEmailNotification() !=null){
            teacher.setEnableEmailNotification(request.getEnableEmailNotification());
        }
        if (request.getAttendanceThreshold() != null) {
            teacher.setAttendanceThreshold(request.getAttendanceThreshold());
        }
        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // 验证密码长度
            if (request.getPassword().length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
            teacher.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getStatus() != null) {
            teacher.setStatus(request.getStatus());
        }

        // 保持college_no不变
        teacher.setCollegeNo(collegeNo);

        teacherService.updateById(teacher);
        log.info("学院{}更新教师成功", collegeNo);
        return Result.success("更新成功");
    }

    /**
     * 删除本学院的教师
     */
    @DeleteMapping("/teachers/{id}")
    @Operation(summary = "删除本学院的教师", description = "学院删除本学院的教师，学院编号从前端传入")
    @SaCheckRole("college_admin")
    public Result<String> deleteTeacher(
            @Parameter(description = "教师ID") @PathVariable String id) {
        String collegeNo = teacherService.getById(id).getCollegeNo();
        log.info("学院{}删除教师，ID：{}", collegeNo, id);

        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }

        // 检查教师是否属于本学院
        if (!collegeNo.equals(teacher.getCollegeNo())) {
            return Result.error("无权限删除该教师");
        }

        teacherService.removeById(id);
        log.info("学院{}删除教师成功", collegeNo);
        return Result.success("删除成功");
    }

    /**
     * 查询教师（分页）
     */
    @PostMapping("/teachers/query")
    @Operation(summary = "分页查询教师", description = "查询维度：学院，教师真名，工号")
    @SaCheckRole(value = {"college_admin","admin"}, mode = SaMode.OR)
    public Result<Page<Teacher>> queryTeachers(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "查询条件") @RequestBody TeacherQueryDTO queryDTO) {
        log.info("学院{}查询教师请求，参数：{}", queryDTO);
        if(StpUtil.hasRole("college_admin")){
            College college = (College) StpUtil.getSession().get("collegeInfo");
            queryDTO.setDepartment(college.getName());
        }
        Page<Teacher> result = teacherService.queryPage(queryDTO);
        log.info("查询教师完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 教师自己的个人信息修改
     */
    @PutMapping("/profile")
    @Operation(summary = "教师自己的个人信息修改", description = "教师自己的个人信息修改")
    @SaCheckRole("teacher")
    public Result<String> updateProfile(@io.swagger.v3.oas.annotations.parameters.RequestBody (description = "教师只能修改用户名，密码，邮箱通知，电话，邮箱") @RequestBody TeacherRequest teacherRequest) {
        // 获取当前登录用户的教师工号
        String teacherNo = StpUtil.getLoginIdAsString();
        Teacher existingTeacher = teacherService.lambdaQuery()
                .eq(Teacher::getTeacherNo, teacherNo)
                .one();

        if (existingTeacher == null) {
                    return Result.error("教师不存在");
                }
        Teacher teacher = new Teacher();
        teacher.setId(existingTeacher.getId());
        teacher.setTeacherNo(teacherNo);

        // 如果提供了新密码，则更新密码
        if (teacherRequest.getPassword() != null && !teacherRequest.getPassword().isEmpty()){
            // 验证密码长度
            if (teacher.getPassword().length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
            teacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));
        }
        if(!StringUtil.isNullOrEmpty(teacherRequest.getEmail())){
            teacher.setEmail(teacherRequest.getEmail());
        }
        if(!StringUtil.isNullOrEmpty(teacherRequest.getPhone())){
            teacher.setPhone(teacherRequest.getPhone());
        }
        if(StringUtil.isNullOrEmpty(teacherRequest.getUsername())){
            teacher.setUsername(teacherRequest.getUsername());
        }
        if(teacherRequest.getEnableEmailNotification() != null){
            teacher.setEnableEmailNotification(teacherRequest.getEnableEmailNotification());
        }
        teacherService.updateById(teacher);
        log.info("教师{}修改个人信息成功", teacherNo);
        return Result.success("修改成功");
    }

    /**
     * 批量导入教师
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入教师", description = "批量导入教师，需要提供工号和真实姓名，其他字段使用默认值，学院编号从前端传入")
    @SaCheckRole("college_admin")
    public Result<String> importTeachers(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "学院编号", required = true) @RequestParam("collegeNo") String collegeNo) {
        log.info("批量导入教师，学院编号：{}", collegeNo);

        if (file.isEmpty()) {
            return Result.error("上传的文件不能为空");
        }

        return teacherService.importTeachers(file, collegeNo);
    }

    /**
     * 生成教师导入模板
     */
    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载教师导入模板", description = "下载教师批量导入的Excel模板")
    @SaCheckRole("college_admin")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        
        String fileName = "教师导入模板.xlsx";
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            fileName = "TeacherTemplate.xlsx";
        }
        
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        
        // 创建模板数据
        List<TeacherExcelDTO> templateData = List.of(new TeacherExcelDTO());
        
        EasyExcel.write(response.getOutputStream(), TeacherExcelDTO.class)
                .sheet("教师导入模板")
                .doWrite(templateData);
    }

    /**
     * 设置老师的考勤阈值字段（以院为单位，若为学校管理员则前端传学院号，若为学院管理员则不用）
     */
    @PutMapping("/setAttendanceThreshold")
    @Operation(summary = "设置老师考勤阈值", description = "设置老师考勤阈值,传学院号")
    @SaCheckRole("college_admin")
    public Result<String> setAttendanceThreshold(@Parameter String collegeNo, @Parameter BigDecimal threshold) {
        log.info("设置老师考勤阈值,学院编号：{}", collegeNo);
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getCollegeNo, collegeNo);
        List<Teacher> teachers = teacherService.list(queryWrapper);
        for (Teacher teacher : teachers) {
            teacher.setAttendanceThreshold(threshold);
            teacherService.updateById(teacher);
        }
        return Result.success("设置成功");
    }

}