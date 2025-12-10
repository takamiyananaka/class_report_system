package com.xuegongbu.controller.admin;

import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.LoginRequest;
import com.xuegongbu.dto.LoginResponse;
import com.xuegongbu.dto.TeacherRequest;
import com.xuegongbu.service.AdminService;
import com.xuegongbu.service.TeacherService;
import com.xuegongbu.vo.TeacherVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "管理员管理")
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
    @ApiOperation(value = "管理员登录", notes = "管理员通过用户名和密码登录系统，返回JWT token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("管理员登录请求: {}", loginRequest.getUsername());
        LoginResponse response = adminService.login(loginRequest);
        return Result.success(response);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    @ApiOperation(value = "管理员登出", notes = "管理员退出登录，清除认证上下文")
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
    @ApiOperation(value = "查询所有教师", notes = "管理员查询所有教师列表")
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
    @ApiOperation(value = "根据ID查询教师", notes = "管理员根据教师ID查询教师详情")
    public Result<TeacherVO> getTeacher(@PathVariable Long id) {
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
    @ApiOperation(value = "创建教师", notes = "管理员创建新教师")
    public Result<String> createTeacher(@Valid @RequestBody TeacherRequest request) {
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
        
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(request, teacher);
        
        // 密码加密
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            // 默认密码
            teacher.setPassword(passwordEncoder.encode("123456"));
        }
        
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
    @ApiOperation(value = "更新教师", notes = "管理员更新教师信息")
    public Result<String> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
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
    @ApiOperation(value = "删除教师", notes = "管理员删除教师")
    public Result<String> deleteTeacher(@PathVariable Long id) {
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
