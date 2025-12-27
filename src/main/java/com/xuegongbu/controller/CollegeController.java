package com.xuegongbu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.dto.TeacherQueryDTO;
import com.xuegongbu.dto.TeacherRequest;
import com.xuegongbu.service.CollegeService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.vo.TeacherVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

@Slf4j
@RestController
@RequestMapping("/college")
@Tag(name = "学院管理", description = "学院相关接口")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 学院登录
     */
    @PostMapping("/login")
    @Operation(summary = "学院登录", description = "学院通过用户名和密码登录系统，返回Token")
    public Result<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "登录请求") @Valid @RequestBody LoginRequest loginRequest) {
        log.info("学院登录请求: {}", loginRequest.getUsername());
        LoginResponse response = collegeService.login(loginRequest);
        return Result.success(response);
    }

    /**
     * 学院登出
     */
    @PostMapping("/logout")
    @Operation(summary = "学院登出", description = "学院退出登录")
    public Result<String> logout() {
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginId();
                log.info("学院登出: 用户ID={}", loginId);
            }
        } catch (Exception e) {
            log.debug("获取登出用户信息失败", e);
        }
        
        StpUtil.logout();
        
        log.info("学院登出成功");
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录学院的collegeNo
     */
    private String getCurrentCollegeNo() {
        if (!StpUtil.isLogin()) {
            throw new com.xuegongbu.common.exception.BusinessException("未登录或登录已过期，请重新登录");
        }
        
        try {
            SaSession session = StpUtil.getSession();
            College college = (College) session.get("userInfo");
            if (college != null && college.getCollegeNo() != null) {
                return college.getCollegeNo();
            }
            throw new com.xuegongbu.common.exception.BusinessException("无法获取当前登录学院信息");
        } catch (Exception e) {
            log.error("无法解析当前登录学院信息: {}", e.getMessage());
            throw new com.xuegongbu.common.exception.BusinessException("无法获取当前登录学院信息");
        }
    }

    /**
     * 查询本学院的所有教师
     */
    @GetMapping("/teachers")
    @Operation(summary = "查询本学院的所有教师", description = "学院查询本学院（college_no匹配）的教师列表")
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
     * 查询本学院的教师（分页）
     */
    @PostMapping("/teachers/query")
    @Operation(summary = "查询本学院的教师", description = "支持按教师工号、部门、真实姓名（模糊）、电话号码查询本学院的教师")
    public Result<Page<Teacher>> queryTeachers(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "查询条件") @RequestBody TeacherQueryDTO queryDTO) {
        String collegeNo = getCurrentCollegeNo();
        log.info("学院{}查询教师请求，参数：{}", collegeNo, queryDTO);
        
        // 创建新的查询DTO，添加college_no过滤条件，避免修改输入参数
        TeacherQueryDTO internalQueryDTO = new TeacherQueryDTO();
        BeanUtils.copyProperties(queryDTO, internalQueryDTO);
        internalQueryDTO.setCollegeNo(collegeNo);
        
        Page<Teacher> result = teacherService.queryPage(internalQueryDTO);
        log.info("学院{}查询教师完成，共{}条记录，当前第{}页", collegeNo, result.getTotal(), result.getCurrent());
        return Result.success(result);
    }
}
