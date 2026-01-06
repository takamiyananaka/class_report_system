package com.xuegongbu.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.College;
import com.xuegongbu.domain.CollegeAdmin;
import com.xuegongbu.dto.CollegeAdminRequest;
import com.xuegongbu.service.CollegeAdminService;
import com.xuegongbu.service.CollegeService;
import com.xuegongbu.vo.CollegeAdminVO;
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

@Slf4j
@RestController
@RequestMapping("/collegeAdmin")
@Tag(name = "学院管理员管理", description = "学院管理员CRUD接口，仅admin可访问")
public class CollegeAdminController {

    @Autowired
    private CollegeAdminService collegeAdminService;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 查询所有学院管理员
     */
    @GetMapping("/collegeAdmins")
    @Operation(summary = "查询所有学院管理员", description = "管理员查询所有学院管理员列表")
    @SaCheckRole("admin")
    public Result<List<CollegeAdminVO>> listCollegeAdmins() {
        log.info("管理员查询所有学院管理员");
        List<CollegeAdmin> collegeAdmins = collegeAdminService.list();
        List<CollegeAdminVO> voList = collegeAdmins.stream().map(collegeAdmin -> {
            CollegeAdminVO vo = new CollegeAdminVO();
            BeanUtils.copyProperties(collegeAdmin, vo);
            // 获取学院名称
            College college = collegeService.getById(collegeAdmin.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
            return vo;
        }).collect(Collectors.toList());
        log.info("查询到{}个学院管理员", voList.size());
        return Result.success(voList);
    }

    /**
     * 根据ID查询学院管理员
     */
    @GetMapping("/collegeAdmins/{id}")
    @Operation(summary = "根据ID查询学院管理员", description = "管理员根据ID查询学院管理员详情")
    @SaCheckRole("admin")
    public Result<CollegeAdminVO> getCollegeAdmin(@Parameter(description = "学院管理员ID") @PathVariable String id) {
        log.info("管理员查询学院管理员详情，ID：{}", id);
        CollegeAdmin collegeAdmin = collegeAdminService.getById(id);
        if (collegeAdmin == null) {
            return Result.error("学院管理员不存在");
        }
        CollegeAdminVO vo = new CollegeAdminVO();
        BeanUtils.copyProperties(collegeAdmin, vo);
        // 获取学院名称
        College college = collegeService.getById(collegeAdmin.getCollegeId());
        if (college != null) {
            vo.setCollegeName(college.getName());
        }
        return Result.success(vo);
    }

    /**
     * 创建学院管理员
     */
    @PostMapping("/collegeAdmins")
    @Operation(summary = "创建学院管理员", description = "管理员创建新学院管理员，密码必须至少6位字符")
    @SaCheckRole("admin")
    public Result<String> createCollegeAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "学院管理员信息") 
            @Valid @RequestBody CollegeAdminRequest request) {
        log.info("管理员创建学院管理员，用户名：{}", request.getUsername());

        // 检查用户名是否已存在
        CollegeAdmin existingByUsername = collegeAdminService.lambdaQuery()
                .eq(CollegeAdmin::getUsername, request.getUsername())
                .one();
        if (existingByUsername != null) {
            return Result.error("用户名已存在");
        }

        // 验证密码
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Result.error("密码不能为空，请设置初始密码");
        }
        if (request.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        // 检查学院是否存在
        College college = collegeService.getById(request.getCollegeId());
        if (college == null) {
            return Result.error("所属学院不存在");
        }

        CollegeAdmin collegeAdmin = new CollegeAdmin();
        BeanUtils.copyProperties(request, collegeAdmin);

        // 密码加密
        collegeAdmin.setPassword(passwordEncoder.encode(request.getPassword()));

        // 默认状态为启用
        if (collegeAdmin.getStatus() == null) {
            collegeAdmin.setStatus(1);
        }

        collegeAdminService.save(collegeAdmin);
        log.info("管理员创建学院管理员成功，ID：{}", collegeAdmin.getId());
        return Result.success("创建成功");
    }

    /**
     * 更新学院管理员
     */
    @PutMapping("/collegeAdmins/{id}")
    @Operation(summary = "更新学院管理员", description = "管理员更新学院管理员信息，如提供新密码则必须至少6位字符")
    @SaCheckRole("admin")
    public Result<String> updateCollegeAdmin(
            @Parameter(description = "学院管理员ID") @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "学院管理员信息") 
            @Valid @RequestBody CollegeAdminRequest request) {
        log.info("管理员更新学院管理员，ID：{}", id);

        CollegeAdmin collegeAdmin = collegeAdminService.getById(id);
        if (collegeAdmin == null) {
            return Result.error("学院管理员不存在");
        }

        // 检查用户名是否被其他学院管理员使用
        CollegeAdmin existingByUsername = collegeAdminService.lambdaQuery()
                .eq(CollegeAdmin::getUsername, request.getUsername())
                .ne(CollegeAdmin::getId, id)
                .one();
        if (existingByUsername != null) {
            return Result.error("用户名已被其他学院管理员使用");
        }

        // 检查学院是否存在
        College college = collegeService.getById(request.getCollegeId());
        if (college == null) {
            return Result.error("所属学院不存在");
        }

        // 更新字段
        collegeAdmin.setUsername(request.getUsername());
        collegeAdmin.setRealName(request.getRealName());
        collegeAdmin.setCollegeId(request.getCollegeId());
        collegeAdmin.setPhone(request.getPhone());
        collegeAdmin.setEmail(request.getEmail());

        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // 验证密码长度
            if (request.getPassword().length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
            collegeAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getStatus() != null) {
            collegeAdmin.setStatus(request.getStatus());
        }

        collegeAdminService.updateById(collegeAdmin);
        log.info("管理员更新学院管理员成功");
        return Result.success("更新成功");
    }

    /**
     * 删除学院管理员
     */
    @DeleteMapping("/collegeAdmins/{id}")
    @Operation(summary = "删除学院管理员", description = "管理员删除学院管理员")
    @SaCheckRole("admin")
    public Result<String> deleteCollegeAdmin(@Parameter(description = "学院管理员ID") @PathVariable String id) {
        log.info("管理员删除学院管理员，ID：{}", id);

        CollegeAdmin collegeAdmin = collegeAdminService.getById(id);
        if (collegeAdmin == null) {
            return Result.error("学院管理员不存在");
        }

        collegeAdminService.removeById(id);
        log.info("管理员删除学院管理员成功");
        return Result.success("删除成功");
    }
}
