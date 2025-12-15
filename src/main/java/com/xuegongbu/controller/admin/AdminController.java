package com.xuegongbu.controller.admin;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.dto.TeacherRequest;
import com.xuegongbu.service.AdminService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.vo.TeacherVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员管理", description = "管理员相关接口")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员通过用户名和密码登录系统，返回JWT token")
    public Result<LoginResponse> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "登录请求") @Valid @RequestBody LoginRequest loginRequest) {
        log.info("管理员登录请求: {}", loginRequest.getUsername());
        LoginResponse response = adminService.login(loginRequest);
        return Result.success(response);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录，清除认证上下文")
    public Result<String> logout() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof Long) {
                    log.info("管理员登出: 用户ID={}", principal);
                } else {
                    log.info("管理员登出: 用户类型={}", principal.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.debug("获取登出用户信息失败", e);
        }
        
        SecurityContextHolder.clearContext();
        
        log.info("管理员登出成功");
        return Result.success("登出成功");
    }

    /**
     * 查询所有教师
     */
    @GetMapping("/teachers")
    @Operation(summary = "查询所有教师", description = "管理员查询所有教师列表")
    public Result<List<TeacherVO>> listTeachers() {
        log.info("管理员查询所有教师");
        List<Teacher> teachers = teacherService.list();
        List<TeacherVO> teacherVOList = teachers.stream().map(teacher -> {
            TeacherVO vo = new TeacherVO();
            BeanUtils.copyProperties(teacher, vo);
            return vo;
        }).collect(Collectors.toList());
        log.info("查询到{}个教师", teacherVOList.size());
        return Result.success(teacherVOList);
    }

    /**
     * 根据ID查询教师
     */
    @GetMapping("/teachers/{id}")
    @Operation(summary = "根据ID查询教师", description = "管理员根据教师ID查询教师详情")
    public Result<TeacherVO> getTeacher(@Parameter(description = "教师ID") @PathVariable Long id) {
        log.info("管理员查询教师详情，ID：{}", id);
        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }
        TeacherVO vo = new TeacherVO();
        BeanUtils.copyProperties(teacher, vo);
        return Result.success(vo);
    }

    /**
     * 创建教师
     */
    @PostMapping("/teachers")
    @Operation(summary = "创建教师", description = "管理员创建新教师，密码必须至少6位字符")
    public Result<String> createTeacher(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @Valid @RequestBody TeacherRequest request) {
        log.info("管理员创建教师，用户名：{}", request.getUsername());
        
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
        
        // 默认状态为启用
        if (teacher.getStatus() == null) {
            teacher.setStatus(1);
        }
        
        teacherService.save(teacher);
        log.info("管理员创建教师成功，ID：{}", teacher.getId());
        return Result.success("创建成功");
    }

    /**
     * 更新教师
     */
    @PutMapping("/teachers/{id}")
    @Operation(summary = "更新教师", description = "管理员更新教师信息，如提供新密码则必须至少6位字符")
    public Result<String> updateTeacher(@Parameter(description = "教师ID") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @Valid @RequestBody TeacherRequest request) {
        log.info("管理员更新教师，ID：{}", id);
        
        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
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
        
        teacherService.updateById(teacher);
        log.info("管理员更新教师成功");
        return Result.success("更新成功");
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/teachers/{id}")
    @Operation(summary = "删除教师", description = "管理员删除教师")
    public Result<String> deleteTeacher(@Parameter(description = "教师ID") @PathVariable Long id) {
        log.info("管理员删除教师，ID：{}", id);
        
        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error("教师不存在");
        }
        
        teacherService.removeById(id);
        log.info("管理员删除教师成功");
        return Result.success("删除成功");
    }
}