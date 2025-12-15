package com.xuegongbu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuegongbu.common.Result;
import com.xuegongbu.domain.Class;
import com.xuegongbu.domain.Teacher;
import com.xuegongbu.dto.ClassQueryDTO;
import com.xuegongbu.service.ClassService;
import com.xuegongbu.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

/**
 * 班级管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/class")
@Tag(name = "班级管理", description = "班级管理接口")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private TeacherService teacherService;

    /**
     * 从当前登录用户获取教师工号
     * @return 教师工号，如果获取失败返回null
     */
    private String getTeacherNoFromAuthentication() {
        try {
            // 从Sa-Token中获取当前用户信息
            if (!StpUtil.isLogin()) {
                return null;
            }

            // 从会话中获取完整的用户信息
            SaSession session = StpUtil.getSession();
            Teacher teacher = (Teacher) session.get("userInfo");
            if (teacher != null) {
                return teacher.getTeacherNo();
            }
            
            // 如果会话中没有用户信息
            Object loginId = StpUtil.getLoginId();
            if (loginId instanceof String) {
                return (String) loginId;
            }
        } catch (Exception e) {
            log.error("无法解析当前登录教师工号: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Excel导入班级
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Excel导入班级", description = "通过上传Excel文件批量导入班级数据。辅导员工号自动从当前登录用户获取。Excel格式要求：第一行为表头，列顺序为：班级名称、班级人数、年级、专业")
    public Result<Map<String, Object>> importFromExcel(
            @Parameter(description = "Excel文件", required = true, 
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("开始导入班级，文件名：{}", file.getOriginalFilename());
            
            // 获取当前登录教师的工号
            String teacherNo = getTeacherNoFromAuthentication();
            if (teacherNo == null) {
                return Result.error("无法获取当前登录用户信息或权限不足");
            }
            
            log.info("当前登录教师工号: {}", teacherNo);
            
            Map<String, Object> result = classService.importFromExcel(file, teacherNo);
            log.info("班级导入完成：{}", result.get("message"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("班级导入失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询班级
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询班级", description = "分页查询班级，支持多条件查询。可通过className（模糊）、teacherNo等参数进行过滤查询。不提供任何条件则查询全部")
    public Result<Page<Class>> query(ClassQueryDTO queryDTO) {
        log.info("查询班级请求，参数：{}", queryDTO);
        Page<Class> result = classService.queryPage(queryDTO);
        log.info("查询班级完成，共{}条记录，当前第{}页", result.getTotal(), result.getCurrent());
        return Result.success(result);
    }

    /**
     * 创建班级
     */
    @PostMapping("/add")
    @Operation(summary = "创建班级", description = "创建新班级，ID自动生成。辅导员工号自动从当前登录用户获取。")
    public Result<Class> addClass(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "班级信息") @RequestBody Class classEntity) {
        log.info("创建班级，班级信息：{}", classEntity);
        
        // 获取当前登录教师的工号
        String teacherNo = getTeacherNoFromAuthentication();
        if (teacherNo == null) {
            return Result.error("无法获取当前登录用户信息或权限不足");
        }
        
        log.info("当前登录教师工号: {}", teacherNo);
        
        // 验证必填字段
        if (classEntity.getClassName() == null || classEntity.getClassName().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }
        if (classEntity.getCount() == null || classEntity.getCount() <= 0) {
            return Result.error("班级人数必须大于0");
        }
        if (classEntity.getGrade() == null || classEntity.getGrade().trim().isEmpty()) {
            return Result.error("年级不能为空");
        }
        if (classEntity.getMajor() == null || classEntity.getMajor().trim().isEmpty()) {
            return Result.error("专业不能为空");
        }
        
        // 自动设置辅导员工号从登录状态
        classEntity.setTeacherNo(teacherNo);
        
        // ID会由MyBatis-Plus自动生成（雪花算法）
        classService.save(classEntity);
        log.info("创建班级完成，班级ID：{}", classEntity.getId());
        return Result.success(classEntity);
    }

    /**
     * 更新班级
     */
    @PutMapping("/update")
    @Operation(summary = "更新班级", description = "通过班级名称更新班级信息")
    public Result<String> updateClass(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "班级信息") @RequestBody Class classEntity) {
        log.info("更新班级，班级名称：{}，班级信息：{}", classEntity.getClassName(), classEntity);
        
        // 检查权限
        if (!StpUtil.isLogin() || StpUtil.hasRole("admin")) {
            return Result.error("权限不足");
        }
        
        if (classEntity.getClassName() == null || classEntity.getClassName().trim().isEmpty()) {
            return Result.error("班级名称不能为空");
        }
        
        // 根据班级名称查询班级
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Class::getClassName, classEntity.getClassName().trim());
        
        Class existing = classService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        // 设置ID以便更新
        classEntity.setId(existing.getId());
        
        classService.updateById(classEntity);
        log.info("更新班级完成");
        return Result.success("更新成功");
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除班级", description = "通过班级名称删除班级（逻辑删除）")
    public Result<String> deleteClass(@Parameter(description = "班级名称", required = true) @RequestParam(value = "className", required = true) String className) {
        log.info("删除班级，班级名称：{}", className);
        
        // 检查权限
        if (!StpUtil.isLogin() || StpUtil.hasRole("admin")) {
            return Result.error("权限不足");
        }
        
        // 根据班级名称查询班级
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Class::getClassName, className.trim());
        
        Class existing = classService.getOne(queryWrapper);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        classService.removeById(existing.getId());
        log.info("删除班级完成");
        return Result.success("删除成功");
    }

    /**
     * 查询班级详情
     */
    @GetMapping("/get")
    @Operation(summary = "查询班级详情", description = "根据班级名称查询班级详情")
    public Result<Class> getClass(@Parameter(description = "班级名称", required = true) @RequestParam(value = "className", required = true) String className) {
        log.info("查询班级详情，班级名称：{}", className);
        
        // 根据班级名称查询班级
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Class> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(Class::getClassName, className.trim());
        
        Class classEntity = classService.getOne(queryWrapper);
        if (classEntity == null) {
            return Result.error("班级不存在");
        }
        log.info("查询班级详情完成");
        return Result.success(classEntity);
    }

    /**
     * 根据ID查询班级详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "根据ID查询班级详情", description = "根据班级ID查询班级详情")
    public Result<Class> getClassById(@Parameter(description = "班级ID") @PathVariable String id) {
        log.info("根据ID查询班级详情，班级ID：{}", id);
        
        Class classEntity = classService.getById(id);
        if (classEntity == null) {
            return Result.error("班级不存在");
        }
        log.info("查询班级详情完成");
        return Result.success(classEntity);
    }

    /**
     * 根据ID更新班级
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "根据ID更新班级", description = "通过班级ID更新班级信息")
    public Result<String> updateClassById(@Parameter(description = "班级ID") @PathVariable String id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "班级信息") @RequestBody Class classEntity) {
        log.info("根据ID更新班级，班级ID：{}，班级信息：{}", id, classEntity);
        
        // 检查权限
        if (!StpUtil.isLogin() || StpUtil.hasRole("admin")) {
            return Result.error("权限不足");
        }
        
        Class existing = classService.getById(id);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        // 设置ID以便更新
        classEntity.setId(id);
        
        classService.updateById(classEntity);
        log.info("更新班级完成");
        return Result.success("更新成功");
    }

    /**
     * 根据ID删除班级
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "根据ID删除班级", description = "通过班级ID删除班级（逻辑删除）")
    public Result<String> deleteClassById(@Parameter(description = "班级ID") @PathVariable String id) {
        log.info("根据ID删除班级，班级ID：{}", id);
        
        // 检查权限
        if (!StpUtil.isLogin() || StpUtil.hasRole("admin")) {
            return Result.error("权限不足");
        }
        
        Class existing = classService.getById(id);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        classService.removeById(id);
        log.info("删除班级完成");
        return Result.success("删除成功");
    }

    /**
     * 下载班级导入模板
     */
    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载班级导入模板", description = "下载Excel格式的班级导入模板文件")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            log.info("下载班级导入模板");
            classService.downloadTemplate(response);
            log.info("下载班级导入模板完成");
        } catch (Exception e) {
            log.error("下载班级导入模板失败", e);
            throw new com.xuegongbu.common.exception.BusinessException("下载模板失败: " + e.getMessage());
        }
    }
}