package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
     * 获取当前登录学院的collegeNo
     */
    private String getCurrentCollegeNo() {
        if (!StpUtil.isLogin()) {
            throw new com.xuegongbu.common.exception.BusinessException("未登录或登录已过期，请重新登录");
        }
        
        // 检查当前用户是否为学院管理员
        if (!StpUtil.hasRole("college_admin")) {
            throw new com.xuegongbu.common.exception.BusinessException("当前用户不是学院管理员，无权限操作");
        }
        
        // 获取当前登录的学院管理员ID
        String collegeAdminId = StpUtil.getLoginIdAsString();
        // 根据ID查询学院管理员信息
        CollegeAdmin currentCollegeAdmin = collegeAdminMapper.selectById(collegeAdminId);
        if (currentCollegeAdmin == null) {
            throw new com.xuegongbu.common.exception.BusinessException("未找到当前登录的学院管理员信息");
        }

        // 获取学院信息
        College college = collegeMapper.selectById(currentCollegeAdmin.getCollegeId());
        if (college == null) {
            throw new com.xuegongbu.common.exception.BusinessException("未找到对应的学院信息");
        }
        
        return college.getCollegeNo();
    }

    /**
     * 查询本学院的所有教师
     */
    @GetMapping("/teachers")
    @Operation(summary = "查询本学院的所有教师", description = "学院查询本学院（college_no匹配）的教师列表")
    @SaCheckRole("college_admin")
    public Result<List<TeacherVO>> listTeachers() {
        String collegeNo = getCurrentCollegeNo();
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
    @Operation(summary = "根据ID查询本学院的教师", description = "学院根据教师ID查询本学院的教师详情")
    @SaCheckRole("college_admin")
    public Result<TeacherVO> getTeacher(@Parameter(description = "教师ID") @PathVariable String id) {
        String collegeNo = getCurrentCollegeNo();
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
    @Operation(summary = "创建本学院的教师", description = "学院创建本学院的新教师，密码必须至少6位字符")
    @SaCheckRole("college_admin")
    public Result<String> createTeacher(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @Valid @RequestBody TeacherRequest request) {
        String collegeNo = getCurrentCollegeNo();
        log.info("学院{}创建教师，用户名：{}", collegeNo, request.getUsername());

        // 检查用户名是否已存在
        Teacher existingTeacher = teacherService.lambdaQuery()
                .eq(Teacher::getUsername, request.getUsername())
                .one();
        if (existingTeacher != null) {
            return Result.error("用户名已存在");
        }

        // 检查教师工号是否已存在
        Teacher existingTeacherNo = teacherService.lambdaQuery()
                .eq(Teacher::getTeacherNo, request.getTeacherNo())
                .one();
        if (existingTeacherNo != null) {
            return Result.error("教师工号已存在");
        }

        // 验证密码
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Result.error("密码不能为空，请设置初始密码");
        }
        if (request.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(request, teacher);

        // 密码加密
        teacher.setPassword(passwordEncoder.encode(request.getPassword()));

        // 设置为本学院
        teacher.setCollegeNo(collegeNo);

        // 默认状态为启用
        if (teacher.getStatus() == null) {
            teacher.setStatus(1);
        }

        teacherService.save(teacher);
        log.info("学院{}创建教师成功，ID：{}", collegeNo, teacher.getId());
        return Result.success("创建成功");
    }

    /**
     * 更新本学院的教师
     */
    @PutMapping("/teachers/{id}")
    @Operation(summary = "更新本学院的教师", description = "学院更新本学院的教师信息，如提供新密码则必须至少6位字符")
    @SaCheckRole("college_admin")
    public Result<String> updateTeacher(@Parameter(description = "教师ID") @PathVariable String id,
                                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @Valid @RequestBody TeacherRequest request) {
        String collegeNo = getCurrentCollegeNo();
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

        // 更新字段
        teacher.setUsername(request.getUsername());
        teacher.setRealName(request.getRealName());
        teacher.setTeacherNo(request.getTeacherNo());
        teacher.setPhone(request.getPhone());
        teacher.setEmail(request.getEmail());
        teacher.setDepartment(request.getDepartment());
        teacher.setAttendanceThreshold(request.getAttendanceThreshold());
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
    @Operation(summary = "删除本学院的教师", description = "学院删除本学院的教师")
    @SaCheckRole("college_admin")
    public Result<String> deleteTeacher(@Parameter(description = "教师ID") @PathVariable String id) {
        String collegeNo = getCurrentCollegeNo();
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
            College college = (College) StpUtil.getSession().get("CollegeInfo");
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
    public Result<String> updateProfile(@RequestBody Teacher teacher) {
        // 获取当前登录用户的教师工号
        String teacherNo = StpUtil.getLoginIdAsString();
        Teacher existingTeacher = teacherService.lambdaQuery()
                .eq(Teacher::getTeacherNo, teacherNo)
                .one();

        if (existingTeacher == null) {
                    return Result.error("教师不存在");
                }

        teacher.setTeacherNo(teacherNo);
        teacher.setId(existingTeacher.getId());
        // 如果提供了新密码，则更新密码
        if (teacher.getPassword() != null && !teacher.getPassword().isEmpty()){
            // 验证密码长度
            if (teacher.getPassword().length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
            teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        }
        teacherService.updateById(teacher);
        log.info("教师{}修改个人信息成功", teacherNo);
        return Result.success("修改成功");
    }

    /**
     * 批量导入教师
     */
    @PostMapping("/import")
    @Operation(summary = "批量导入教师", description = "批量导入教师，需要提供工号和真实姓名，其他字段使用默认值")
    @SaCheckRole("college_admin")
    public Result<String> importTeachers(@RequestParam("file") MultipartFile file) {
        String collegeNo = getCurrentCollegeNo();
        log.info("学院{}批量导入教师", collegeNo);

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
}