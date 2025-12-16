package com.xuegongbu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.TeacherQueryDTO;
import com.xuegongbu.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/teacher")
@Tag(name = "教师管理", description = "教师相关接口")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 多条件查询教师
     */
    @PostMapping("/query")
    @Operation(summary = "多条件查询教师", description = "支持按教师工号、部门、真实姓名（模糊）、电话号码查询。不提供任何条件则查询全部")
    public Result<Page<Teacher>> query(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "查询条件") @RequestBody TeacherQueryDTO queryDTO) {
        log.info("查询教师请求，参数：{}", queryDTO);
        Page<Teacher> result = teacherService.queryPage(queryDTO);
        log.info("查询教师完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 根据教师工号查询教师
     */
    @GetMapping("/getByTeacherNo/{teacherNo}")
    @Operation(summary = "根据教师工号查询教师", description = "根据教师工号查询教师详情")
    public Result<Teacher> getByTeacherNo(@Parameter(description = "教师工号") @PathVariable String teacherNo) {
        log.info("查询教师详情，教师工号：{}", teacherNo);
        
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getTeacherNo, teacherNo);
        Teacher teacher = teacherService.getOne(queryWrapper);
        
        if (teacher == null) {
            return Result.error("教师不存在");
        }
        
        // 移除密码字段
        teacher.setPassword(null);
        
        log.info("查询教师详情完成");
        return Result.success(teacher);
    }

    /**
     * 创建教师
     */
    @PostMapping("/add")
    @Operation(summary = "创建教师", description = "创建新教师，ID自动生成")
    public Result<Teacher> addTeacher(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @RequestBody Teacher teacher) {
        log.info("创建教师，教师信息：{}", teacher);
        
        // 验证必填字段
        if (teacher.getUsername() == null || teacher.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (teacher.getPassword() == null || teacher.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (teacher.getRealName() == null || teacher.getRealName().trim().isEmpty()) {
            return Result.error("真实姓名不能为空");
        }
        if (teacher.getTeacherNo() == null || teacher.getTeacherNo().trim().isEmpty()) {
            return Result.error("教师工号不能为空");
        }
        
        // 检查教师工号是否已存在
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getTeacherNo, teacher.getTeacherNo());
        if (teacherService.count(queryWrapper) > 0) {
            return Result.error("教师工号已存在");
        }
        
        // 加密密码
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        
        // ID会由MyBatis-Plus自动生成（雪花算法）
        teacherService.save(teacher);
        
        // 移除返回的密码字段
        teacher.setPassword(null);
        
        log.info("创建教师完成，教师ID：{}", teacher.getId());
        return Result.success(teacher);
    }

    /**
     * 更新教师
     */
    @PutMapping("/update")
    @Operation(summary = "更新教师", description = "更新教师信息，通过教师工号定位")
    public Result<String> updateTeacher(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "教师信息") @RequestBody Teacher teacher) {
        log.info("更新教师，教师工号：{}，教师信息：{}", teacher.getTeacherNo(), teacher);
        
        if (teacher.getTeacherNo() == null || teacher.getTeacherNo().trim().isEmpty()) {
            return Result.error("教师工号不能为空");
        }
        
        // 根据教师工号查询教师
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getTeacherNo, teacher.getTeacherNo());
        Teacher existingTeacher = teacherService.getOne(queryWrapper);
        
        if (existingTeacher == null) {
            return Result.error("教师不存在");
        }
        
        // 更新教师信息（使用查询到的ID）
        teacher.setId(existingTeacher.getId());
        
        // 如果提供了新密码，则加密
        if (teacher.getPassword() != null && !teacher.getPassword().trim().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        } else {
            // 不更新密码
            teacher.setPassword(null);
        }
        
        teacherService.updateById(teacher);
        log.info("更新教师完成");
        return Result.success("更新成功");
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/delete/{teacherNo}")
    @Operation(summary = "删除教师", description = "通过教师工号删除教师（逻辑删除）")
    public Result<String> deleteTeacher(@Parameter(description = "教师工号") @PathVariable String teacherNo) {
        log.info("删除教师，教师工号：{}", teacherNo);
        
        // 根据教师工号查询教师
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teacher::getTeacherNo, teacherNo);
        Teacher teacher = teacherService.getOne(queryWrapper);
        
        if (teacher == null) {
            return Result.error("教师不存在");
        }
        
        teacherService.removeById(teacher.getId());
        log.info("删除教师完成");
        return Result.success("删除成功");
    }
}